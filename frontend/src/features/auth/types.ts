export type User = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  roles: string[];
  emailVerified: boolean;
};

export type AuthResponse = {
  accessToken: string | null;
  expiresInSeconds: number;
  user: User;
};
