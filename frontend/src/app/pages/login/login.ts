import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';


import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  username = '';
  password = '';

  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {
    if (this.route.snapshot.queryParamMap.get('registered') === 'true') {
      this.successMessage = 'Registration successful! Please sign in.';
    }
  }

  onLogin(): void {
    if (this.isLoading) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    if (!this.username.trim() || !this.password) {
      this.errorMessage = 'Please enter your username and password.';
      return;
    }

    this.isLoading = true;

    this.authService.login({
      username: this.username.trim(),
      password: this.password
    }).subscribe({
      next: response => {
        if (!response.token) {
          this.errorMessage =
            'Login failed: the server did not return an access token.';
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }

        this.isLoading = false;
        this.successMessage = 'Login successful! Redirecting...';
        this.cdr.detectChanges();

        this.router.navigate(['/dashboard']);
      },

      error: error => {
        console.error('Login request failed:', error);

        if (error.status === 401) {
          const backendMessage = error.error?.message;

          this.errorMessage =
            backendMessage === 'Login Failed'
              ? 'Invalid username or password. Please try again.'
              : backendMessage || 'Authentication failed. Please sign in again.';

        } else if (error.status === 403) {
          this.errorMessage =
            error.error?.message ||
            'Access denied. Please contact your administrator.';

        } else if (error.status === 0) {
          this.errorMessage =
            'Cannot connect to the server. Please check whether the backend is running.';

        } else {
          this.errorMessage =
            error.error?.message ||
            'Something went wrong. Please try again.';
        }

        // Explicitly reset the loading state.
        this.isLoading = false;

        // Immediately update the UI.
        this.cdr.detectChanges();
      }
    });
  }
}