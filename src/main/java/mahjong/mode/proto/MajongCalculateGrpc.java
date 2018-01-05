package mahjong.mode.proto;

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
        comments = "Source: majong_rpc.proto")
public final class MajongCalculateGrpc {

    private MajongCalculateGrpc() {
    }

    public static final String SERVICE_NAME = "majong_rpc.MajongCalculate";

    // Static method descriptors that strictly reflect the proto.
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<mahjong.mode.proto.CalculateData,
            mahjong.mode.proto.CalculateResult> METHOD_CALCULATE =
            io.grpc.MethodDescriptor.<mahjong.mode.proto.CalculateData, mahjong.mode.proto.CalculateResult>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "majong_rpc.MajongCalculate", "calculate"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            mahjong.mode.proto.CalculateData.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            mahjong.mode.proto.CalculateResult.getDefaultInstance()))
                    .build();
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<mahjong.mode.proto.SettleData,
            mahjong.mode.proto.SettleResult> METHOD_SETTLE =
            io.grpc.MethodDescriptor.<mahjong.mode.proto.SettleData, mahjong.mode.proto.SettleResult>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "majong_rpc.MajongCalculate", "settle"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            mahjong.mode.proto.SettleData.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            mahjong.mode.proto.SettleResult.getDefaultInstance()))
                    .build();
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<mahjong.mode.proto.ShuffleData,
            mahjong.mode.proto.ShuffleResult> METHOD_SHUFFLE =
            io.grpc.MethodDescriptor.<mahjong.mode.proto.ShuffleData, mahjong.mode.proto.ShuffleResult>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "majong_rpc.MajongCalculate", "shuffle"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            mahjong.mode.proto.ShuffleData.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            mahjong.mode.proto.ShuffleResult.getDefaultInstance()))
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
         * 进行麻将牌过程计算
         * </pre>
         */
        public void calculate(mahjong.mode.proto.CalculateData request,
                              io.grpc.stub.StreamObserver<mahjong.mode.proto.CalculateResult> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_CALCULATE, responseObserver);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public void settle(mahjong.mode.proto.SettleData request,
                           io.grpc.stub.StreamObserver<mahjong.mode.proto.SettleResult> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_SETTLE, responseObserver);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public void shuffle(mahjong.mode.proto.ShuffleData request,
                            io.grpc.stub.StreamObserver<mahjong.mode.proto.ShuffleResult> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_SHUFFLE, responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            METHOD_CALCULATE,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            mahjong.mode.proto.CalculateData,
                                            mahjong.mode.proto.CalculateResult>(
                                            this, METHODID_CALCULATE)))
                    .addMethod(
                            METHOD_SETTLE,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            mahjong.mode.proto.SettleData,
                                            mahjong.mode.proto.SettleResult>(
                                            this, METHODID_SETTLE)))
                    .addMethod(
                            METHOD_SHUFFLE,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            mahjong.mode.proto.ShuffleData,
                                            mahjong.mode.proto.ShuffleResult>(
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
         * 进行麻将牌过程计算
         * </pre>
         */
        public void calculate(mahjong.mode.proto.CalculateData request,
                              io.grpc.stub.StreamObserver<mahjong.mode.proto.CalculateResult> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_CALCULATE, getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public void settle(mahjong.mode.proto.SettleData request,
                           io.grpc.stub.StreamObserver<mahjong.mode.proto.SettleResult> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_SETTLE, getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public void shuffle(mahjong.mode.proto.ShuffleData request,
                            io.grpc.stub.StreamObserver<mahjong.mode.proto.ShuffleResult> responseObserver) {
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
         * 进行麻将牌过程计算
         * </pre>
         */
        public mahjong.mode.proto.CalculateResult calculate(mahjong.mode.proto.CalculateData request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_CALCULATE, getCallOptions(), request);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public mahjong.mode.proto.SettleResult settle(mahjong.mode.proto.SettleData request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_SETTLE, getCallOptions(), request);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public mahjong.mode.proto.ShuffleResult shuffle(mahjong.mode.proto.ShuffleData request) {
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
         * 进行麻将牌过程计算
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<mahjong.mode.proto.CalculateResult> calculate(
                mahjong.mode.proto.CalculateData request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_CALCULATE, getCallOptions()), request);
        }

        /**
         * <pre>
         * 结算
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<mahjong.mode.proto.SettleResult> settle(
                mahjong.mode.proto.SettleData request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_SETTLE, getCallOptions()), request);
        }

        /**
         * <pre>
         * 洗牌函数
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<mahjong.mode.proto.ShuffleResult> shuffle(
                mahjong.mode.proto.ShuffleData request) {
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
                    serviceImpl.calculate((mahjong.mode.proto.CalculateData) request,
                            (io.grpc.stub.StreamObserver<mahjong.mode.proto.CalculateResult>) responseObserver);
                    break;
                case METHODID_SETTLE:
                    serviceImpl.settle((mahjong.mode.proto.SettleData) request,
                            (io.grpc.stub.StreamObserver<mahjong.mode.proto.SettleResult>) responseObserver);
                    break;
                case METHODID_SHUFFLE:
                    serviceImpl.shuffle((mahjong.mode.proto.ShuffleData) request,
                            (io.grpc.stub.StreamObserver<mahjong.mode.proto.ShuffleResult>) responseObserver);
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
            return mahjong.mode.proto.MajongRpcProto.getDescriptor();
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
