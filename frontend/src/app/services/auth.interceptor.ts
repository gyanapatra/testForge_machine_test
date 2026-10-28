import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const isPublicAuthEndpoint =
    request.url.includes('/api/auth/login') ||
    request.url.includes('/api/auth/register');

  // Login and registration must not send an old JWT.
  if (isPublicAuthEndpoint) {
    return next(request);
  }

  const token = localStorage.getItem('access_token');

  // Attach JWT only when a token exists.
  if (token && request.url.startsWith('http://localhost:8080/')) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(request);
};
