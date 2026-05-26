import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

export default function JoinPage() {
  return (
    <main className="flex min-h-screen items-center justify-center p-6 bg-background">
      <div className="absolute inset-0 z-0 h-full w-full bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:16px_16px] [mask-image:radial-gradient(ellipse_50%_50%_at_50%_50%,#000_70%,transparent_100%)] dark:bg-[radial-gradient(#1f2937_1px,transparent_1px)]" />

      <Card className="z-10 w-full max-w-md shadow-xl border-none bg-card/80 backdrop-blur-md">
        <CardHeader>
          <CardTitle className="text-2xl font-bold text-center">Join a Flat</CardTitle>
          <CardDescription className="text-center">Enter the invite code from your roommates.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="inviteCode">Invite Code</Label>
            <Input id="inviteCode" type="text" placeholder="e.g. FLAT-1234" required className="text-center text-xl tracking-widest h-14" />
          </div>
          <Link href="/dashboard" className="w-full block">
            <Button className="w-full h-12 text-lg">Join Flat</Button>
          </Link>
        </CardContent>
        <CardFooter className="flex flex-col space-y-4">
          <Link href="/" className="text-sm text-muted-foreground hover:underline">
            Back to Home
          </Link>
        </CardFooter>
      </Card>
    </main>
  )
}
