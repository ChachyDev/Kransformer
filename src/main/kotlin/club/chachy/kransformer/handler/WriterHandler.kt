package club.chachy.kransformer.handler

import org.objectweb.asm.ClassWriter

data class WriterHandler(val transformedName: String, val writer: ClassWriter)
