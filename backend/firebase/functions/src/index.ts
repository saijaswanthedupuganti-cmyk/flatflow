import * as functions from "firebase-functions/v1";
import { logger } from "firebase-functions";
import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore, type Firestore, type Query } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

/**
 * DPDP erasure backstop. Runs on Firebase's side whenever an Auth account is deleted, from any
 * client, so a user's personal and Discover data can't be left behind by a crashed or modified app.
 *
 * Flat membership is NOT handled here: leaving a flat hands tasks to the next person and may
 * promote a new admin, which the client does (UsersRepository.deleteUserData) before deleting Auth.
 *
 * Kept on purpose: messages other people sent this user (their record) and discoveryReports
 * (retained up to 2 years for abuse handling, per the Privacy Policy).
 */
export const purgeDeletedUser = functions
  .region("asia-south1")
  .auth.user()
  .onDelete(async (user) => {
    await purgeUserData(getFirestore(), user.uid);
  });

/**
 * Points a newly added member's profile at the flat. Covers admin approval of a join request,
 * where the admin's client isn't allowed to write the requester's users/{uid} doc. Self-joins
 * already wrote it, so this is a harmless no-op merge for them.
 */
export const linkMemberToUser = functions
  .region("asia-south1")
  .firestore.document("flats/{flatId}/members/{uid}")
  .onCreate(async (_snap, context) => {
    const { flatId, uid } = context.params;
    const db = getFirestore();
    const userRef = db.collection("users").doc(uid);
    await db.runTransaction(async (tx) => {
      const user = await tx.get(userRef);
      const update: Record<string, unknown> = { flatIds: FieldValue.arrayUnion(flatId) };
      if (!user.get("activeFlatId")) update.activeFlatId = flatId;
      tx.set(userRef, update, { merge: true });
    });
  });

/**
 * Tells a member when the admin approves or declines the vacancy they posted. Runs on the server
 * because only the requester's own client can read their users/{uid} doc (where the FCM token is).
 */
export const notifyVacancyDecision = functions
  .region("asia-south1")
  .firestore.document("flats/{flatId}/vacancyRequests/{uid}")
  .onUpdate(async (change, context) => {
    const before = change.before.get("status");
    const after = change.after.get("status");
    if (before !== "pending" || (after !== "approved" && after !== "declined")) return;

    const { flatId, uid } = context.params;
    const db = getFirestore();
    const [user, flat] = await Promise.all([
      db.collection("users").doc(uid).get(),
      db.collection("flats").doc(flatId).get(),
    ]);
    const token = user.get("fcmToken");
    if (typeof token !== "string" || token.length === 0) {
      logger.info("notifyVacancyDecision: no FCM token", { uid });
      return;
    }
    const flatName = (flat.get("name") as string | undefined) || "your flat";
    const approved = after === "approved";
    const title = approved ? "Your room is live on Discover" : "Vacancy not approved";
    const body = approved
      ? `Your admin approved the vacancy for ${flatName}. People can now find it and connect.`
      : `Your admin didn't approve the vacancy for ${flatName}. Open My posts to edit and resend it.`;
    try {
      await getMessaging().send({
        token,
        // Data-only so HabitiqFcmService builds the notification the same way in every app state.
        data: { title, body, type: approved ? "vacancy_approved" : "vacancy_declined", flatId },
        android: { priority: "high" },
      });
    } catch (error) {
      const code = (error as { code?: string }).code;
      // A stale token: drop it so the next login saves a fresh one.
      if (code === "messaging/registration-token-not-registered" || code === "messaging/invalid-registration-token") {
        await db.collection("users").doc(uid).update({ fcmToken: FieldValue.delete() });
      }
      logger.warn("notifyVacancyDecision: send failed", { uid, code });
    }
  });

export async function purgeUserData(db: Firestore, uid: string): Promise<void> {
  const failures: string[] = [];
  // Each step runs on its own so one failure doesn't strand the rest.
  const step = async (name: string, run: () => Promise<unknown>) => {
    try {
      await run();
    } catch (error) {
      failures.push(name);
      logger.error(`purgeDeletedUser: ${name} failed`, { uid, error });
    }
  };

  await step("seekerProfile", () => db.collection("seekerProfiles").doc(uid).delete());
  // Recursive so the blocked/ subcollection goes with the user doc.
  await step("userDoc", () => db.recursiveDelete(db.collection("users").doc(uid)));
  await step("connectionsFrom", () =>
    deleteMatching(db, db.collection("discoveryConnections").where("fromUid", "==", uid)));
  await step("connectionsTo", () =>
    deleteMatching(db, db.collection("discoveryConnections").where("toUid", "==", uid)));
  await step("sentMessages", () =>
    deleteMatching(db, db.collection("messages").where("senderId", "==", uid)));

  if (failures.length > 0) {
    // Throwing marks the run as failed in Cloud Logging so a manual cleanup isn't missed.
    throw new Error(`purgeDeletedUser incomplete for ${uid}: ${failures.join(", ")}`);
  }
  logger.info("purgeDeletedUser: done", { uid });
}

async function deleteMatching(db: Firestore, query: Query): Promise<void> {
  const writer = db.bulkWriter();
  const snap = await query.get();
  snap.docs.forEach((doc) => writer.delete(doc.ref));
  await writer.close();
}
