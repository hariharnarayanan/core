package net.securustech.embs.zookeeper.lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.util.List;

import static net.securustech.embs.zookeeper.ZookeeperConstants.ZOOKEEPER_PATH_EMBSLOCK;
import static net.securustech.embs.zookeeper.ZookeeperConstants.ZOOKEEPR_PATH_SEPARATOR;

@Aspect
@Order(1)
@Component
public class EmbsLockAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbsLockAspect.class);

    @Autowired
    private CuratorFramework curatorFramework;

    @Around("@annotation(embsLock)")
    public Object lock(ProceedingJoinPoint pjp, EmbsLock embsLock) {

        Object result = null;

        try {

            String localServerHost = InetAddress.getLocalHost().getHostName();
            LOGGER.debug("EMBSLock@ParentPath :-> " + ZOOKEEPER_PATH_EMBSLOCK);
            LOGGER.debug("EMBSLock@LocalServerHost :-> " + localServerHost);
            List<String> embsLockChildren;

            if (curatorFramework.checkExists().forPath(ZOOKEEPER_PATH_EMBSLOCK) == null) {

                LOGGER.debug("EMBSLock@ParentPath ###CREATING### ::: " +
                        ZOOKEEPER_PATH_EMBSLOCK + ZOOKEEPR_PATH_SEPARATOR + localServerHost);

                createEMBSLock(ZOOKEEPER_PATH_EMBSLOCK, localServerHost);

            } else {

                LOGGER.debug("EMBSLock@ParentPath ###EXISTING### ::: " + ZOOKEEPER_PATH_EMBSLOCK);
                embsLockChildren = curatorFramework.getChildren().forPath(ZOOKEEPER_PATH_EMBSLOCK);
                LOGGER.debug("EMBSLock@ParentPath Children :-> " + embsLockChildren);
                if (!CollectionUtils.isEmpty(embsLockChildren)) {

                    if (!StringUtils.equalsIgnoreCase(localServerHost, embsLockChildren.get(0))) {

                        LOGGER.warn("EMBSLock@Children@LocalServerHost :-> " + embsLockChildren.get(0));
                        throw new KeeperException.NodeExistsException(ZOOKEEPER_PATH_EMBSLOCK + ZOOKEEPR_PATH_SEPARATOR +
                                embsLockChildren.get(0));
                    }
                } else {

                    LOGGER.warn("EMBSLock@ParentPath ###MISSING### ###MISSING### ###MISSING### Children For ::: " +
                            ZOOKEEPER_PATH_EMBSLOCK);
                    LOGGER.warn("EMBSLock@ParentPath ###CREATING### Children ::: " +
                            ZOOKEEPER_PATH_EMBSLOCK + ZOOKEEPR_PATH_SEPARATOR + localServerHost);

                    createEMBSLock(ZOOKEEPER_PATH_EMBSLOCK, localServerHost);
                }
            }

            //Proceed with the Calling Method Execution
            result = pjp.proceed();

            curatorFramework.setData().forPath(ZOOKEEPER_PATH_EMBSLOCK + ZOOKEEPR_PATH_SEPARATOR + localServerHost,
                    (localServerHost + ZOOKEEPR_PATH_SEPARATOR + System.currentTimeMillis()).getBytes());

        } catch (KeeperException.NodeExistsException ex) {

            LOGGER.warn("################# EMBS LOCK{} <<< " + ZOOKEEPER_PATH_EMBSLOCK + " >>> already exists!!! #################", ex);
        } catch (Throwable e) {

            LOGGER.warn("################# Exception while Acquiring EMBS LOCK{}!!! May be <<< " +
                    ZOOKEEPER_PATH_EMBSLOCK + " >>> exists!!! #################", e);
        }

        return result;
    }

    private void createEMBSLock(String embsLockParentPath, String localServerHost) throws Exception {

        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(
                embsLockParentPath + ZOOKEEPR_PATH_SEPARATOR + localServerHost,
                (localServerHost + ZOOKEEPR_PATH_SEPARATOR + System.currentTimeMillis()).getBytes());

    }
}
