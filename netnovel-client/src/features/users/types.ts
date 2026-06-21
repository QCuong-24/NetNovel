export type UserProfile = {
  userId: number;
  username: string;
  profilePictureUrl?: string | null;
  roles?: string[];
  createAt?: string;
};
