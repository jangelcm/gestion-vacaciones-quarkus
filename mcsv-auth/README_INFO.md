Perfecto 👍. Aquí tienes el mapa completo de propiedades JWT cuando trabajas con Quarkus, diferenciando claramente entre:

MicroProfile JWT Auth
SmallRye JWT
Propiedades propias de Quarkus

1. Propiedades de MicroProfile JWT (mp.jwt.*)

Estas vienen del estándar MicroProfile y funcionan en cualquier implementación compatible.

Verificación de tokens
mp.jwt.verify.publickey.location
mp.jwt.verify.publickey
mp.jwt.verify.issuer
mp.jwt.verify.audiences
mp.jwt.verify.clock.skew
mp.jwt.verify.token.age

ejemplo
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem
mp.jwt.verify.issuer=https://auth.miempresa.com
mp.jwt.verify.clock.skew=60


2. Propiedades de SmallRye JWT (smallrye.jwt.*)
Estas pertenecen a SmallRye JWT y extienden el estándar.

Para firmar tokens

smallrye.jwt.sign.key.location
smallrye.jwt.sign.key
smallrye.jwt.sign.key.id
smallrye.jwt.sign.algorithm

smallrye.jwt.sign.key.location=META-INF/resources/privateKey.pem
smallrye.jwt.sign.algorithm=RS256


Otras propiedades de SmallRye

smallrye.jwt.new-token.lifespan
smallrye.jwt.new-token.issuer
smallrye.jwt.new-token.audience
smallrye.jwt.time-to-live
smallrye.jwt.required.claims
smallrye.jwt.path.groups


3. Propiedades propias de Quarkus (quarkus.smallrye-jwt.*)

Estas propiedades pertenecen a la integración de Quarkus con SmallRye JWT.

Configuración del token

quarkus.smallrye-jwt.enabled
quarkus.smallrye-jwt.realm-name
quarkus.smallrye-jwt.token.header
quarkus.smallrye-jwt.token.prefix


ejemplo

quarkus.smallrye-jwt.token.header=Authorization
quarkus.smallrye-jwt.token.prefix=Bearer



Cookies

quarkus.smallrye-jwt.token.cookie


ejemplo
quarkus.smallrye-jwt.token.cookie=jwt


autenticacion
quarkus.smallrye-jwt.auth-mechanism
quarkus.smallrye-jwt.always-check-authorization



configuracion en produccion

# validar token
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem
mp.jwt.verify.issuer=https://auth.miempresa.com

# firmar token
smallrye.jwt.sign.key.location=META-INF/resources/privateKey.pem
smallrye.jwt.sign.algorithm=RS256

# configuración quarkus
quarkus.smallrye-jwt.token.header=Authorization
quarkus.smallrye-jwt.token.prefix=Bearer