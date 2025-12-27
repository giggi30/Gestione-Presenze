#!/bin/bash

# Script per generare le chiavi RSA per JWT (Private e Public)
# Necessario per la configurazione del file .env

echo "Generazione coppia di chiavi RSA (2048 bit)..."

# 1. Genera chiave privata PEM
openssl genrsa -out private.pem 2048 > /dev/null 2>&1

# 2. Converti chiave privata in formato PKCS8 DER (richiesto da Java)
openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem -out private.der -nocrypt > /dev/null 2>&1

# 3. Estrai chiave pubblica in formato DER
openssl rsa -in private.pem -pubout -outform DER -out public.der > /dev/null 2>&1

# 4. Codifica in Base64 su una singola riga (senza newline)
PRIVATE_KEY_BASE64=$(base64 < private.der | tr -d '\n')
PUBLIC_KEY_BASE64=$(base64 < public.der | tr -d '\n')

# 5. Pulizia file temporanei
rm private.pem private.der public.der

# 6. Output per l'utente
echo "----------------------------------------------------------------"
echo "Chiavi generate con successo!"
echo "----------------------------------------------------------------"
echo "Copia e incolla le seguenti righe nel tuo file .env:"
echo ""
echo "JWT_PRIVATE_KEY=$PRIVATE_KEY_BASE64"
echo ""
echo "JWT_PUBLIC_KEY=$PUBLIC_KEY_BASE64"
echo ""
echo "JWT_EXPIRATION=3600"
echo "----------------------------------------------------------------"
