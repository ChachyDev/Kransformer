# Transformers-DSL

Transformers DSL is a simple project that allows for an easy start to using transformers with Forge with instruction building powered by Koffee.

### Example Transformer:

```kotlin
class MyModTransformer : ClassTransformer() {
    init {
        transform("net.minecraft.client.Minecraft") {
            val startGame = method("startGame") ?: return@transform

            insert(startGame) {
                getstatic(System::class, "out", PrintStream::class)
                ldc("Hello, World!")
                invokevirtual(PrintStream::class, "println", void, String::class)
            }
        }
    }
}
```