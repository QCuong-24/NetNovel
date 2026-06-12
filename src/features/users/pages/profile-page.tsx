import { Mail, Shield, UserRound } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ImageUploader } from '@/features/uploads/components/image-uploader';
import { useUploadUserAvatarMutation } from '@/features/uploads/hooks/use-image-upload';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';

export function ProfilePage() {
  const { data: user, isLoading } = useCurrentUser();
  const uploadAvatarMutation = useUploadUserAvatarMutation();

  if (isLoading) {
    return (
      <main className="mx-auto grid w-full max-w-5xl gap-6 px-4 py-6 md:px-6">
        <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
          Loading profile...
        </div>
      </main>
    );
  }

  if (!user) {
    return (
      <main className="mx-auto grid w-full max-w-5xl gap-6 px-4 py-6 md:px-6">
        <Card>
          <CardContent className="p-6">
            <p className="font-semibold">Profile is unavailable.</p>
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="mx-auto grid w-full max-w-5xl gap-6 px-4 py-6 md:px-6">
      <div>
        <p className="text-sm font-semibold uppercase tracking-wide text-primary">Profile</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">{user.username}</h1>
      </div>

      <section className="grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]">
        <Card>
          <CardHeader>
            <CardTitle>Avatar</CardTitle>
          </CardHeader>
          <CardContent>
            <ImageUploader
              buttonLabel="Upload avatar"
              currentImageUrl={user.profilePictureUrl}
              description="JPG, PNG, or WebP. The backend stores the Cloudinary metadata for your account."
              emptyLabel="No avatar"
              isUploading={uploadAvatarMutation.isPending}
              previewClassName="aspect-square rounded-full"
              title="Profile avatar"
              onUpload={(file) => uploadAvatarMutation.mutateAsync(file)}
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Account</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4">
            <div className="flex items-center gap-3 rounded-md border bg-background p-4">
              <UserRound className="size-5 text-primary" />
              <div>
                <p className="text-xs font-semibold uppercase text-muted-foreground">Username</p>
                <p className="font-semibold">{user.username}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-md border bg-background p-4">
              <Mail className="size-5 text-primary" />
              <div>
                <p className="text-xs font-semibold uppercase text-muted-foreground">Email</p>
                <p className="font-semibold">{user.email}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-md border bg-background p-4">
              <Shield className="size-5 text-primary" />
              <div>
                <p className="text-xs font-semibold uppercase text-muted-foreground">Roles</p>
                <p className="font-semibold">{user.roles?.join(', ') || 'USER'}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>
    </main>
  );
}
