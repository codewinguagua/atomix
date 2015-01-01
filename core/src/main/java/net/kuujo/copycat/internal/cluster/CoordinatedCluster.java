/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.copycat.internal.cluster;

import net.kuujo.copycat.cluster.Cluster;
import net.kuujo.copycat.cluster.LocalMember;
import net.kuujo.copycat.cluster.Member;
import net.kuujo.copycat.cluster.coordinator.ClusterCoordinator;
import net.kuujo.copycat.cluster.coordinator.MemberCoordinator;
import net.kuujo.copycat.election.Election;
import net.kuujo.copycat.internal.CopycatStateContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Stateful cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CoordinatedCluster implements Cluster {
  private final int id;
  private CoordinatedLocalMember localMember;
  private final Map<String, CoordinatedMember> members = new HashMap<>();
  private final CoordinatedClusterElection election;
  private final Router router;
  private final CopycatStateContext context;

  public CoordinatedCluster(int id, ClusterCoordinator coordinator, CopycatStateContext context, Router router, Executor executor) {
    this.id = id;
    this.localMember = new CoordinatedLocalMember(id, coordinator.member(), executor);
    this.members.put(localMember.uri(), localMember);
    for (MemberCoordinator member : coordinator.members()) {
      if (!member.uri().equals(localMember.uri())) {
        this.members.put(member.uri(), new CoordinatedMember(id, member, executor));
      }
    }
    this.election = new CoordinatedClusterElection(this, context);
    this.router = router;
    this.context = context;
  }

  @Override
  public Member leader() {
    return context.getLeader() != null ? member(context.getLeader()) : null;
  }

  @Override
  public long term() {
    return context.getTerm();
  }

  @Override
  public Election election() {
    return election;
  }

  @Override
  public Member member(String uri) {
    return members.get(uri);
  }

  @Override
  public LocalMember member() {
    return localMember;
  }

  @Override
  public Collection<Member> members() {
    return Collections.unmodifiableCollection(members.values());
  }

  @Override
  public CompletableFuture<Cluster> open() {
    router.createRoutes(this, context);
    election.open();
    return localMember.open().thenApply(m -> this);
  }

  @Override
  public boolean isOpen() {
    return localMember.isOpen();
  }

  @Override
  public CompletableFuture<Void> close() {
    localMember.close();
    router.destroyRoutes(this, context);
    election.close();
    return localMember.close();
  }

  @Override
  public boolean isClosed() {
    return localMember.isClosed();
  }

  @Override
  public String toString() {
    return String.format("%s[members=%s]", getClass().getCanonicalName(), members());
  }

}
