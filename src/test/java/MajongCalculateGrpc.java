import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.6.1)",
        comments = "Source: paohuzi.proto")
public final class MajongCalculateGrpc {

    private MajongCalculateGrpc() {
    }

    public static final String SERVICE_NAME = "MajongCalculate";

    // Static method descriptors that strictly reflect the proto.
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<Paohuzi.CalculateData,
            Paohuzi.CalculateResult> METHOD_CALCULATE =
            io.grpc.MethodDescriptor.<Paohuzi.CalculateData, Paohuzi.CalculateResult>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "MajongCalculate", "calculate"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            Paohuzi.CalculateData.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            Paohuzi.CalculateResult.getDefaultInstance()))
                    .build();
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<Paohuzi.SettleData,
            Paohuzi.SettleResult> METHOD_SETTLE =
            io.grpc.MethodDescriptor.<Paohuzi.SettleData, Paohuzi.SettleResult>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "MajongCalculate", "settle"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            Paohuzi.SettleData.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            Paohuzi.SettleResult.getDefaultInstance()))
                    .build();
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<Paohuzi.ShuffleData,
            Paohuzi.ShuffleResult> METHOD_SHUFFLE =
            io.grpc.MethodDescriptor.<Paohuzi.ShuffleData, Paohuzi.ShuffleResult>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "MajongCalculate", "shuffle"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            Paohuzi.ShuffleData.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            Paohuzi.ShuffleResult.getDefaultInstance()))
                    .build();

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static MajongCalculateStub newStub(io.grpc.Channel channel) {
        return new MajongCalculateStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static MajongCalculateBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new MajongCalculateBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static MajongCalculateFutureStub newFutureStub(
            io.grpc.Channel channel) {
        return new MajongCalculateFutureStub(channel);
    }

    /**
     */
    public static abstract class MajongCalculateImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         * 进行过程计算
         * </pre>
         */
        public void calculate(Paohuzi.CalculateData request,
                              io.grpc.stub.StreamObserver<Paohuzi.CalculateResult> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_CALCULATE, responseObserver);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public void settle(Paohuzi.SettleData request,
                           io.grpc.stub.StreamObserver<Paohuzi.SettleResult> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_SETTLE, responseObserver);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public void shuffle(Paohuzi.ShuffleData request,
                            io.grpc.stub.StreamObserver<Paohuzi.ShuffleResult> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_SHUFFLE, responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            METHOD_CALCULATE,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            Paohuzi.CalculateData,
                                            Paohuzi.CalculateResult>(
                                            this, METHODID_CALCULATE)))
                    .addMethod(
                            METHOD_SETTLE,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            Paohuzi.SettleData,
                                            Paohuzi.SettleResult>(
                                            this, METHODID_SETTLE)))
                    .addMethod(
                            METHOD_SHUFFLE,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            Paohuzi.ShuffleData,
                                            Paohuzi.ShuffleResult>(
                                            this, METHODID_SHUFFLE)))
                    .build();
        }
    }

    /**
     */
    public static final class MajongCalculateStub extends io.grpc.stub.AbstractStub<MajongCalculateStub> {
        private MajongCalculateStub(io.grpc.Channel channel) {
            super(channel);
        }

        private MajongCalculateStub(io.grpc.Channel channel,
                                    io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected MajongCalculateStub build(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
            return new MajongCalculateStub(channel, callOptions);
        }

        /**
         * <pre>
         * 进行过程计算
         * </pre>
         */
        public void calculate(Paohuzi.CalculateData request,
                              io.grpc.stub.StreamObserver<Paohuzi.CalculateResult> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_CALCULATE, getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public void settle(Paohuzi.SettleData request,
                           io.grpc.stub.StreamObserver<Paohuzi.SettleResult> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_SETTLE, getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public void shuffle(Paohuzi.ShuffleData request,
                            io.grpc.stub.StreamObserver<Paohuzi.ShuffleResult> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_SHUFFLE, getCallOptions()), request, responseObserver);
        }
    }

    /**
     */
    public static final class MajongCalculateBlockingStub extends io.grpc.stub.AbstractStub<MajongCalculateBlockingStub> {
        private MajongCalculateBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private MajongCalculateBlockingStub(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected MajongCalculateBlockingStub build(io.grpc.Channel channel,
                                                    io.grpc.CallOptions callOptions) {
            return new MajongCalculateBlockingStub(channel, callOptions);
        }

        /**
         * <pre>
         * 进行过程计算
         * </pre>
         */
        public Paohuzi.CalculateResult calculate(Paohuzi.CalculateData request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_CALCULATE, getCallOptions(), request);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public Paohuzi.SettleResult settle(Paohuzi.SettleData request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_SETTLE, getCallOptions(), request);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public Paohuzi.ShuffleResult shuffle(Paohuzi.ShuffleData request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_SHUFFLE, getCallOptions(), request);
        }
    }

    /**
     */
    public static final class MajongCalculateFutureStub extends io.grpc.stub.AbstractStub<MajongCalculateFutureStub> {
        private MajongCalculateFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private MajongCalculateFutureStub(io.grpc.Channel channel,
                                          io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected MajongCalculateFutureStub build(io.grpc.Channel channel,
                                                  io.grpc.CallOptions callOptions) {
            return new MajongCalculateFutureStub(channel, callOptions);
        }

        /**
         * <pre>
         * 进行过程计算
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<Paohuzi.CalculateResult> calculate(
                Paohuzi.CalculateData request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_CALCULATE, getCallOptions()), request);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<Paohuzi.SettleResult> settle(
                Paohuzi.SettleData request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_SETTLE, getCallOptions()), request);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<Paohuzi.ShuffleResult> shuffle(
                Paohuzi.ShuffleData request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_SHUFFLE, getCallOptions()), request);
        }
    }

    private static final int METHODID_CALCULATE = 0;
    private static final int METHODID_SETTLE = 1;
    private static final int METHODID_SHUFFLE = 2;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final MajongCalculateImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(MajongCalculateImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_CALCULATE:
                    serviceImpl.calculate((Paohuzi.CalculateData) request,
                            (io.grpc.stub.StreamObserver<Paohuzi.CalculateResult>) responseObserver);
                    break;
                case METHODID_SETTLE:
                    serviceImpl.settle((Paohuzi.SettleData) request,
                            (io.grpc.stub.StreamObserver<Paohuzi.SettleResult>) responseObserver);
                    break;
                case METHODID_SHUFFLE:
                    serviceImpl.shuffle((Paohuzi.ShuffleData) request,
                            (io.grpc.stub.StreamObserver<Paohuzi.ShuffleResult>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }

    private static final class MajongCalculateDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return Paohuzi.getDescriptor();
        }
    }

    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (MajongCalculateGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new MajongCalculateDescriptorSupplier())
                            .addMethod(METHOD_CALCULATE)
                            .addMethod(METHOD_SETTLE)
                            .addMethod(METHOD_SHUFFLE)
                            .build();
                }
            }
        }
        return result;
    }
}
