// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.lock.store;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.preparation.Identifiable;

public class AbstractLockedResourceRepositoryTest extends LockedResourceTestUtils {

    protected LockedResourceRepository repository;

    @Before
    public void setUp() {
        repository.clear();
    }

    @After
    public void tearDown() {
        repository.clear();
    }

    @Test
    public void should_lock_unlocked_resource() {
        String userId = "1";
        Identifiable resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, userId);
        long now = Instant.now().getEpochSecond();

        assertNotNull(lockedResource);
        assertEquals(userId, lockedResource.getUserId());
        assertEquals(resource.getId(), lockedResource.getResourceId());
        assertTrue(now <= lockedResource.getExpirationTime());

        assertTrue(lockedResource.getExpirationTime() < (now + 60 * 11));
    }

    @Test
    public void should_lock_unlocked_resource_and_then_unlock_it() {
        String userId = "1";
        Identifiable resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, userId);
        final LockedResource mustBeNull = repository.tryUnlock(resource, userId);

        assertNotNull(lockedResource);
        assertNull(mustBeNull);
        assertTrue(repository.lockReleased(mustBeNull));
    }

    @Test
    public void should_unlock_unlocked_resource() {
        String userId = "1";
        Identifiable resource = getFirstResourceType("1");

        final LockedResource mustBeNull = repository.tryUnlock(resource, userId);

        assertNull(mustBeNull);
        assertTrue(repository.lockReleased(mustBeNull));
    }

    @Test
    public void lock_should_be_reentrant() {
        String lockOwner = "1";
        Identifiable resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, lockOwner);
        LockedResource lockedResource2 = repository.tryLock(resource, lockOwner);

        assertNotNull(lockedResource);
        assertNotNull(lockedResource2);
        assertEquals(lockedResource.getUserId(), lockedResource2.getUserId());
        assertEquals(lockedResource.getResourceId(), lockedResource2.getResourceId());
        assertTrue(lockedResource.getExpirationTime() <= lockedResource2.getExpirationTime());
    }

    @Test
    public void should_not_lock_resource_already_locked_by_another_user() {
        String owner = "1";
        String preEmpter = "2";
        Identifiable resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, owner);
        LockedResource lockedByPreviousUser = repository.tryLock(resource, preEmpter);

        assertNotNull(lockedResource);
        assertEquals(lockedByPreviousUser, lockedResource);
    }

    @Test
    public void should_list_all_locked_resources() {
        String user1 = "1";
        String user2 = "2";
        Identifiable resource = getFirstResourceType("1");
        Identifiable resource2 = getSecondResourceType("2");
        Identifiable resource3 = getSecondResourceType("3");

        LockedResource lockOnResource1 = repository.tryLock(resource, user1);
        LockedResource lockOnResource2 = repository.tryLock(resource2, user2);
        repository.tryLock(resource3, user2);
        repository.tryUnlock(resource3, user2);

        Collection<LockedResource> allLockedResources = repository.listAll();

        assertNotNull(allLockedResources);
        assertEquals(2, allLockedResources.size());
        assertTrue(allLockedResources.contains(lockOnResource1));
        assertTrue(allLockedResources.contains(lockOnResource2));
    }

    @Test
    public void should_list_all_locked_resources_by_a_user() {
        String user1 = "1";
        String user2 = "2";
        Identifiable resource = getFirstResourceType("1");
        Identifiable resource2 = getSecondResourceType("2");
        Identifiable resource3 = getSecondResourceType("3");

        LockedResource lockOnResource1 = repository.tryLock(resource, user1);
        LockedResource lockOnResource2 = repository.tryLock(resource2, user2);
        LockedResource lockOnResource3 = repository.tryLock(resource3, user2);

        Collection<LockedResource> allLockedResources = repository.listByUser(user2);

        assertNotNull(allLockedResources);
        assertEquals(2, allLockedResources.size());
        assertFalse(allLockedResources.contains(lockOnResource1));
        assertTrue(allLockedResources.contains(lockOnResource2));
        assertTrue(allLockedResources.contains(lockOnResource3));
    }

}