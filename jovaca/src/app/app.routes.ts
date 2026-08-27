import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'solicitudes', pathMatch: 'full' },
  {
    path: 'solicitudes',
    loadComponent: () =>
      import('./vacaciones/listado/listado.component').then(m => m.ListadoComponent)
  },
  {
    path: 'solicitudes/nueva',
    loadComponent: () =>
      import('./vacaciones/formulario/formulario.component').then(m => m.FormularioComponent)
  },
  {
    path: 'solicitudes/:id',
    loadComponent: () =>
      import('./vacaciones/detalle/detalle.component').then(m => m.DetalleComponent)
  }
];
