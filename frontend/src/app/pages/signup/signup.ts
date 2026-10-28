import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import {
  AuthService,
  SignupRequest,
  VerifyOtpRequest
} from '../../services/auth';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup.html',
  styleUrl: './signup.css'
})
export class Signup {

  username = '';
  email = '';
  password = '';
  userType: 'STANDARD_USER' | 'ADMINISTRATOR' = 'STANDARD_USER';
  countryCode = 'IN';

  otp = '';
  otpStep = false;

  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  onSignup(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.username.trim() || !this.email.trim() || !this.password) {
      this.errorMessage = 'Please complete all required fields.';
      return;
    }

    if (this.username.trim().length < 8 ||
      this.username.trim().length > 50) {
      this.errorMessage = 'Username must contain 8–50 characters.';
      return;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailPattern.test(this.email.trim())) {
      this.errorMessage = 'Please enter a valid email address.';
      return;
    }

    const passwordPattern =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,12}$/;

    if (!passwordPattern.test(this.password)) {
      this.errorMessage =
        'Password must be 8–12 characters and contain uppercase, lowercase, a number, and a special character.';
      return;
    }

    const request: SignupRequest = {
      username: this.username.trim(),
      email: this.email.trim().toLowerCase(),
      password: this.password,
      userType: this.userType,
      countryCode: this.countryCode
    };

    this.isLoading = true;
    this.cdr.detectChanges();

    this.authService.signup(request).subscribe({
      next: response => {
        console.log('Signup successful:', response);

        this.otpStep = true;
        this.password = '';
        this.otp = '';
        this.errorMessage = '';
        this.successMessage =
          response.message ||
          'OTP generated. Check the Auth Service console.';

        this.isLoading = false;

        // Force Angular to update the displayed form.
        this.cdr.detectChanges();

        console.log('OTP verification screen activated:', this.otpStep);
      },

      error: error => {
        console.error('Signup failed:', error);

        this.errorMessage =
          error.error?.message ||
          'Registration failed. Please check your details and try again.';

        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onVerifyOtp(): void {
    this.errorMessage = '';
    this.successMessage = '';

    const normalizedOtp = this.otp.trim();

    if (!/^\d{6}$/.test(normalizedOtp)) {
      this.errorMessage = 'Please enter the 6-digit OTP.';
      return;
    }

    const request: VerifyOtpRequest = {
      email: this.email.trim().toLowerCase(),
      otp: normalizedOtp
    };

    this.isLoading = true;

    this.authService.verifyOtp(request).subscribe({
      next: response => {
        this.isLoading = false;

        this.successMessage =
          response.message ||
          'Email verified and account created successfully.';

        setTimeout(() => {
          this.router.navigate(['/login'], {
            queryParams: { registered: 'true' }
          });
        }, 1200);
      },
      error: error => {
        this.errorMessage =
          error.error?.message ||
          'OTP verification failed. Please try again.';

        this.isLoading = false;
      }
    });
  }

  backToSignup(): void {
    this.otpStep = false;
    this.otp = '';
    this.errorMessage = '';
    this.successMessage = '';
  }
}