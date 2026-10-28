import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {

  username = 'User';
  role = 'STANDARD_USER';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Read the stored user details after dependency injection.
    this.username = this.authService.getUsername() ?? 'User';
    this.role = this.authService.getRole() ?? 'STANDARD_USER';
  }

  get isAdmin(): boolean {
    return this.role === 'ADMINISTRATOR';
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.clearSession();
        this.router.navigate(['/login']);
      },
      error: error => {
        console.error('Logout request failed:', error);

        // Clear the local session if the server request fails.
        this.authService.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }
}