# XIKMAD AI

A native Android chat-app foundation with a WhatsApp-inspired interface.

## Implemented in this starter
- Compose chat UI with outgoing/incoming bubbles and timestamps
- Multiple local conversations: create, switch, search, delete
- Message history persisted locally on the device
- Somali/English interface toggle
- GitHub Actions workflow to build and upload a debug APK artifact

## Important AI limitation
This starter uses a small offline demo responder. It is **not yet a generative AI model**. A compatible on-device model runtime, model file compatibility checks, and real-device performance tests must be added before claiming full offline AI capability.

## Build
Use JDK 17, Android SDK 35, then run `gradle assembleDebug`.
