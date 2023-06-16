import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.Invoker

val emptyMockInvoker = Invoker(ffiInvoker = ConfigBuilder().build().ffiInvoker)
