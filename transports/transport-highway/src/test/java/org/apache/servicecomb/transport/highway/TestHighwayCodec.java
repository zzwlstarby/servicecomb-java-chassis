/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.highway;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootDeserializer;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootSerializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.vertx.server.TcpParser;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import mockit.Mocked;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TestHighwayCodec {
  private OperationProtobuf operationProtobuf = null;

  private Buffer bodyBuffer = null;

  private RequestRootSerializer requestSerializer = null;

  private RequestRootDeserializer<Object> requestRootDeserializer = null;

  private SchemaMeta schemaMeta = null;

  private OperationMeta operationMeta = null;

  private MicroserviceMeta microserviceMeta = null;

  private ByteBuf lByteBuf = null;

  private ByteBuffer nioBuffer = null;

  private Invocation invocation = null;

  @BeforeClass
  public static void setupClass() {
  }

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    operationProtobuf = Mockito.mock(OperationProtobuf.class);

    bodyBuffer = Mockito.mock(Buffer.class);

    requestSerializer = Mockito.mock(RequestRootSerializer.class);

    requestRootDeserializer = Mockito.mock(RequestRootDeserializer.class);

    schemaMeta = Mockito.mock(SchemaMeta.class);

    operationMeta = Mockito.mock(OperationMeta.class);

    microserviceMeta = Mockito.mock(MicroserviceMeta.class);

    lByteBuf = Mockito.mock(ByteBuf.class);

    nioBuffer = Mockito.mock(ByteBuffer.class);

    invocation = Mockito.mock(Invocation.class);
  }

  @After
  public void tearDown() {

    operationProtobuf = null;

    bodyBuffer = null;

    requestSerializer = null;

    schemaMeta = null;

    operationMeta = null;

    lByteBuf = null;

    nioBuffer = null;

    invocation = null;
  }


  @Test
  public void testDecodeRequestTraceId(@Mocked Endpoint endpoint) throws Exception {
    commonMock();

    Invocation invocation = new Invocation(endpoint, operationMeta, null);

    invocation.addContext("X-B3-traceId", "test1");
    Assert.assertEquals("test1", invocation.getContext("X-B3-traceId"));

    RequestHeader headers = new RequestHeader();
    Map<String, String> context = new HashMap<>();
    headers.setContext(context);
    HighwayCodec.decodeRequest(invocation, headers, operationProtobuf, bodyBuffer);
    Assert.assertEquals("test1", invocation.getContext("X-B3-traceId"));

    context.put("X-B3-traceId", "test2");
    HighwayCodec.decodeRequest(invocation, headers, operationProtobuf, bodyBuffer);
    Assert.assertEquals("test2", invocation.getContext("X-B3-traceId"));
  }

  @Test
  public void testEncodeResponse() {
    boolean status = true;
    ResponseRootSerializer bodySchema = Mockito.mock(ResponseRootSerializer.class);
    try {
      commonMock();
      Object data = new Object();
      Mockito.when(bodySchema.serialize(data)).thenReturn(new byte[0]);
      HighwayCodec.encodeResponse(23432142, null, bodySchema, data);
    } catch (Exception e) {
      e.printStackTrace();
      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testEncodeRequest() {
    boolean status = true;
    try {
      commonMock();
      Map<String, Object> args = new HashMap<>(0);
      Mockito.when(invocation.getInvocationArguments()).thenReturn(args);
      Mockito.when(requestSerializer.serialize(args)).thenReturn(new byte[0]);
      TcpOutputStream os = HighwayCodec.encodeRequest(0, invocation, operationProtobuf);
      Assert.assertNotNull(os);
      Assert.assertArrayEquals(TcpParser.TCP_MAGIC, os.getBuffer().getBytes(0, 7));
    } catch (Exception e) {
      e.printStackTrace();
      status = false;
    }
    Assert.assertTrue(status);
  }

  private void commonMock() {
    Mockito.when(operationProtobuf.getRequestRootSerializer()).thenReturn(requestSerializer);
    Mockito.when(operationProtobuf.getRequestRootDeserializer()).thenReturn(requestRootDeserializer);
    Mockito.when(bodyBuffer.getByteBuf()).thenReturn(lByteBuf);
    Mockito.when(bodyBuffer.getBytes()).thenReturn(new byte[0]);
    Mockito.when(lByteBuf.nioBuffer()).thenReturn(nioBuffer);
    Mockito.when(operationProtobuf.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
  }
}
