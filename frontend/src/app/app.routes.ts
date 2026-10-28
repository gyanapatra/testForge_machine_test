import { Routes } from '@angular/router';

import { Login } from './pages/login/login';
import { Signup } from './pages/signup/signup';
import { Dashboard } from './pages/dashboard/dashboard';
import { Products } from './pages/products/products';
import { Settings } from './pages/settings/settings';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'signup', component: Signup },
  { path: 'dashboard', component: Dashboard },
  { path: 'products', component: Products },
  { path: 'settings', component: Settings },
  { path: '**', redirectTo: 'login' }
];