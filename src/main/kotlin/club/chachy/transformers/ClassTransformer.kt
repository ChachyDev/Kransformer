package club.chachy.transformers

import club.chachy.transformers.dsl.TransformerBuilder
import com.google.common.collect.ArrayListMultimap
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

open class ClassTransformer : IClassTransformer {
    // ArrayListMultiMap to hold transformer data
    private val transformers: ArrayListMultimap<String, TransformerBuilder.() -> Unit> = ArrayListMultimap.create()

    /**
     * Transform a given class.
     *
     * @param name Name of class
     * @param transformedName Transformed name of class
     * @param bytes Bytes of class
     */

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

    /**
     * Load transformer block into a list mapped to the class name.
     *
     * @param name Name of the class to be transformed
     * @param transformer The transformer block where your transformations are.
     */

    fun transform(vararg name: String, transformer: TransformerBuilder.() -> Unit) {
        name.forEach {
            transformers.put(it, transformer)
        }
    }
}