package club.chachy.transformers.dsl

import codes.som.anthony.koffee.BlockAssembly
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*

class TransformerBuilder(val classNode: ClassNode, val name: String?) {
    fun method(name: String) = method(classNode, name)

    fun method(classNode: ClassNode, name: String) = classNode.methods.firstOrNull { it.name == name }

    fun method(node: (MethodNode) -> Boolean) = method(classNode, node)

    fun method(classNode: ClassNode, node: (MethodNode) -> Boolean) = classNode.methods.firstOrNull(node)

    fun field(name: String) = field(classNode, name)

    fun field(classNode: ClassNode, name: String) = classNode.fields.firstOrNull { it.name == name }

    fun clazz(name: String): ClassNode {
        return ClassNode().also {
            ClassReader(name).also { reader ->
                reader.accept(it, ClassReader.EXPAND_FRAMES)
            }
        }
    }

    inline fun <reified T : AbstractInsnNode> MethodNode.findInstruction(predicate: (T) -> Boolean): T? {
        for (insn in instructions) {
            if (insn is T && predicate(insn)) return insn
        }

        return null
    }

    fun insertBefore(methodNode: MethodNode, insnNode: AbstractInsnNode, block: BlockAssembly.() -> Unit) =
        methodNode.instructions.insertBefore(insnNode, BlockAssembly(InsnList(), ArrayList()).apply(block).instructions)

    fun insert(methodNode: MethodNode, block: BlockAssembly.() -> Unit) =
        methodNode.instructions.insert(BlockAssembly(InsnList(), ArrayList()).apply(block).instructions)

    fun insert(name: String, block: BlockAssembly.() -> Unit) =
        method(name)?.instructions?.insert(BlockAssembly(InsnList(), ArrayList()).apply(block).instructions)

    fun insert(methodNode: MethodNode, insnNode: AbstractInsnNode, block: BlockAssembly.() -> Unit) =
        methodNode.instructions.insert(insnNode, BlockAssembly(InsnList(), ArrayList()).apply(block).instructions)

    fun insertReturn(methodNode: MethodNode, block: BlockAssembly.() -> Unit) {
        methodNode.instructions.insertBefore(
            methodNode.instructions.last.previous,
            BlockAssembly(InsnList(), ArrayList())
                .apply(block)
                .instructions
        )
    }


    fun mapMethodName(classNode: ClassNode, methodNode: MethodNode): String =
        FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, methodNode.name, methodNode.desc)

    fun mapFieldName(classNode: ClassNode, fieldNode: FieldNode): String =
        FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(classNode.name, fieldNode.name, fieldNode.desc)

    fun mapMethodDesc(methodNode: MethodNode): String =
        FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(methodNode.desc)

    fun mapFieldNameFromNode(fieldInsnNode: FieldInsnNode): String =
        FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc)

    fun clearInstructions(methodNode: MethodNode) {
        methodNode.instructions.clear()
        methodNode.localVariables.clear()
        methodNode.tryCatchBlocks.clear()
    }
}