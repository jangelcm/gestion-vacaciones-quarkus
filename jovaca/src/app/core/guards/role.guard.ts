import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export function roleGuard(role: string): CanActivateFn {
    return () => {
        const auth = inject(AuthService);
        if (auth.hasRole(role)) {
            return true;
        }
        return inject(Router).createUrlTree(['/solicitudes']);
    };
}
