export type User = {
  userId: number;
  username: string;
  email: string;
  profilePictureUrl?: string | null;
  profilePicturePublicId?: string | null;
  roles?: string[];
  provider?: string;
  createAt?: string;
};

export type AuthResponse = {
  user: User;
  accessToken: string;
  refreshToken: string;
  message: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
  profilePictureUrl?: string;
};

export type GoogleLoginRequest = {
  idToken: string;
};
