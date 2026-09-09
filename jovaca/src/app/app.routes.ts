import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', redirectTo: 'solicitudes', pathMatch: 'full' },
      {
        path: 'solicitudes',
        loadComponent: () =>
          import('./vacaciones/listado/listado.component').then(m => m.ListadoComponent)
      },
      {
        path: 'consultas',
        loadComponent: () =>
          import('./consultas/historial/historial.component').then(m => m.HistorialComponent)
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard('Administrador')],
        loadComponent: () =>
          import('./admin/usuarios/listado/usuarios-listado.component').then(m => m.UsuariosListadoComponent)
      },
      {
        path: 'aprobaciones',
        canActivate: [roleGuard('Administrador')],
        loadComponent: () =>
          import('./admin/aprobaciones/listado/aprobaciones-listado.component').then(m => m.AprobacionesListadoComponent)
      },
      {
        path: 'politicas',
        canActivate: [roleGuard('Administrador')],
        loadComponent: () =>
          import('./admin/politicas/listado/politicas-listado.component').then(m => m.PoliticasListadoComponent)
      },
      {
        path: 'reportes',
        canActivate: [roleGuard('Administrador')],
        loadComponent: () =>
          import('./admin/reportes/resumen/reportes-resumen.component').then(m => m.ReportesResumenComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
