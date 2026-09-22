// El gateway siempre corre en el puerto 8080, pero el HOST varia segun donde se sirva el
// frontend (localhost en desarrollo, la IP/dominio del servidor en produccion). Hardcodear
// "localhost" rompe el login apenas se accede desde otra maquina: el navegador del usuario
// intentaria llamar a SU PROPIO localhost:8080, no al servidor real.
const apiHost = typeof window !== 'undefined' ? window.location.hostname : 'localhost';

export const environment = {
  apiBaseUrl: `http://${apiHost}:8080`
};
