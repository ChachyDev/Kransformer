package club.chachy.transformers

import club.chachy.transformers.dsl.TransformerBuilder
import com.google.common.collect.ArrayListMultimap
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode


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