export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
}