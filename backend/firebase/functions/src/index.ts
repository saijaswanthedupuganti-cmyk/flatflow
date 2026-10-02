import * as functions from "firebase-functions/v1";
import { logger } from "firebase-functions";
import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore, type Firestore, type Query } from "firebase-admin/firestore";

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
