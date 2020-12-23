package club.chachy.transformers

import club.chachy.transformers.dsl.TransformerBuilder
import codes.som.anthony.koffee.insns.jvm.getstatic
import codes.som.anthony.koffee.insns.jvm.invokevirtual
import codes.som.anthony.koffee.insns.jvm.ldc
import com.google.common.collect.ArrayListMultimap
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.PrintStream


open class ClassTransformer : IClassTransformer {
    private val transformers: ArrayListMultimap<String, TransformerBuilder.() -> Unit> = ArrayListMultimap.create()

    override fun transform(name: String?, transformedName: String?, bytes: ByteArray?): ByteArray? {
        if (bytes == null) return null

        val clazzTransformers = transformers[transformedName] ?: return null

        if (clazzTransformers.isEmpty()) return bytes

        val node = ClassNode()

        ClassReader(bytes).also {
            it.accept(node, ClassReader.EXPAND_FRAMES)
        }

        clazzTransformers.forEach {
            it(TransformerBuilder(node, transformedName))
        }

        return ClassWriter(ClassWriter.COMPUTE_FRAMES).also {
            node.accept(it)
        }.toByteArray()
    }

    fun transform(vararg name: String, transformer: TransformerBuilder.() -> Unit) {
        name.forEach {
            transformers.put(it, transformer)
        }
    }
}

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
