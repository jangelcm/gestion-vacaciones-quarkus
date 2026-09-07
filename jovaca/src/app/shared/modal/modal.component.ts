import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'app-modal',
    standalone: true,
    imports: [],
    templateUrl: './modal.component.html',
    styleUrl: './modal.component.css'
})
export class ModalComponent {
    @Input() titulo = '';
    @Input() anchoMax = '440px';
    @Output() cerrar = new EventEmitter<void>();

    onBackdropClick(): void {
        this.cerrar.emit();
    }
}
