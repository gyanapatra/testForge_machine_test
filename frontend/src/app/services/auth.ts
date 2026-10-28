import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  message: string;
  token: string | null;
  username: string;
  role: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  userType: 'STANDARD_USER' | 'ADMINISTRATOR';
  countryCode: string;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export interface SignupResponse {
  message: string;
  token: string | null;
  username: string;
  role: string;
}



@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      request
    ).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('access_token', response.token);
          localStorage.setItem('username', response.username);
          localStorage.setItem('role', response.role);
        }
      })
    );
  }

signup(request: SignupRequest): Observable<SignupResponse> {
  return this.http.post<SignupResponse>(
    `${this.apiUrl}/signup`,
    request
  );
}

verifyOtp(request: VerifyOtpRequest): Observable<SignupResponse> {
  return this.http.post<SignupResponse>(
    `${this.apiUrl}/verify-otp`,
    request
  );
}


  getToken(): string | null {
    return localStorage.getItem('access_token');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    const token = this.getToken();

    const headers = token
        ? new HttpHeaders({
            Authorization: `Bearer ${token}`
        })
        : undefined;

    return this.http.post(
        `${this.apiUrl}/logout`,
        {},
        {
        headers,
        responseType: 'text'
        }
    );
}

clearSession(): void {
  localStorage.removeItem('access_token');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
}
}