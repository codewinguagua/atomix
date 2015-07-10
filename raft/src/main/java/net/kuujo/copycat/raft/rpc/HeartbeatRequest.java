/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.raft.rpc;

import net.kuujo.alleycat.Alleycat;
import net.kuujo.alleycat.SerializeWith;
import net.kuujo.alleycat.io.BufferInput;
import net.kuujo.alleycat.io.BufferOutput;
import net.kuujo.alleycat.util.ReferenceManager;

import java.util.Objects;

/**
 * Protocol heartbeat request.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@SerializeWith(id=260)
public class HeartbeatRequest extends AbstractRequest<HeartbeatRequest> {
  private static final ThreadLocal<Builder> builder = new ThreadLocal<Builder>() {
    @Override
    protected Builder initialValue() {
      return new Builder();
    }
  };

  /**
   * Returns a new heartbeat request builder.
   *
   * @return A new heartbeat request builder.
   */
  public static Builder builder() {
    return builder.get().reset();
  }

  /**
   * Returns a heartbeat request builder for an existing request.
   *
   * @param request The request to build.
   * @return The heartbeat request builder.
   */
  public static Builder builder(HeartbeatRequest request) {
    return builder.get().reset(request);
  }

  private int member;

  public HeartbeatRequest(ReferenceManager<HeartbeatRequest> referenceManager) {
    super(referenceManager);
  }

  @Override
  public Type type() {
    return Type.HEARTBEAT;
  }

  /**
   * Returns the requesting member's ID.
   *
   * @return The requesting member's ID.
   */
  public int member() {
    return member;
  }

  @Override
  public void readObject(BufferInput buffer, Alleycat alleycat) {
    member = alleycat.readObject(buffer);
  }

  @Override
  public void writeObject(BufferOutput buffer, Alleycat alleycat) {
    alleycat.writeObject(member, buffer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(member);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof HeartbeatRequest) {
      HeartbeatRequest request = (HeartbeatRequest) object;
      return request.member == member;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%s[member=%s]", getClass().getSimpleName(), member);
  }

  /**
   * Heartbeat request builder.
   */
  public static class Builder extends AbstractRequest.Builder<Builder, HeartbeatRequest> {

    private Builder() {
      super(HeartbeatRequest::new);
    }

    @Override
    Builder reset() {
      super.reset();
      request.member = 0;
      return this;
    }

    /**
     * Sets the requesting node.
     *
     * @param member The requesting node.
     * @return The heartbeat request builder.
     */
    public Builder withMember(int member) {
      if (member <= 0)
        throw new NullPointerException("member must be positive");
      request.member = member;
      return this;
    }

    @Override
    public HeartbeatRequest build() {
      super.build();
      if (request.member <= 0)
        throw new NullPointerException("member must be positive");
      return request;
    }

    @Override
    public int hashCode() {
      return Objects.hash(request);
    }

    @Override
    public boolean equals(Object object) {
      return object instanceof Builder && ((Builder) object).request.equals(request);
    }

    @Override
    public String toString() {
      return String.format("%s[request=%s]", getClass().getCanonicalName(), request);
    }

  }

}
