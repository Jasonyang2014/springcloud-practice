package com.auyeung.seata.service.impl;

import com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler;
import com.auyeung.seata.entity.User;
import com.auyeung.seata.mapper.UserMapper;
import com.auyeung.seata.service.UserService;
import com.auyeung.sentinel.api.OrderApi;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.transaction.TransactionHookAdapter;
import io.seata.tm.api.transaction.TransactionHookManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final OrderApi orderApi;

    /**
     * 转账
     *
     * @param sId     源用户id
     * @param tId     目标用户id
     * @param balance 金额
     * @return 是否成功
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public boolean transfer(Integer sId, Integer tId, BigDecimal balance) {
        boolean s = userMapper.addBalance(tId, balance);
        boolean t = userMapper.subBalance(sId, balance);
        return s && t;
    }

    /**
     * 根据id获取用户
     *
     * @param id id
     * @return
     */
    @Override
    public User getUser(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 创建订单
     * 使用Sentinel做服务熔断和降级，会调用SentinelInvocationHandler#invoke,
     * SentinelInvocationHandler
     * <a href="https://github.com/alibaba/spring-cloud-alibaba/pull/3786/files#diff-a10a7e3094ac1a97299e2d51e0fc6425090434dcbc0644c360eee7877d354a33">fallback Method throw exception but seata does not rollback</a>
     * <pre>
     *      {@code @Override
     * 	public Object invoke(final Object proxy, final Method method, final Object[] args)
     * 			throws Throwable {
     * 		// ....
     * 		Object result;
     * 		MethodHandler methodHandler = this.dispatch.get(method);
     * 		// only handle by HardCodedTarget
     * 		if (target instanceof Target.HardCodedTarget) {
     * 			// ....
     * 				Entry entry = null;
     * 				try {
     * 					ContextUtil.enter(resourceName);
     * 					entry = SphU.entry(resourceName, EntryType.OUT, 1, args);
     * 					result = methodHandler.invoke(args);
     *                }
     * 				catch (Throwable ex) {
     * 					// fallback handle
     *
     * 					if (fallbackFactory != null) {
     * 						try {
     * 							Object fallbackResult = fallbackMethodMap.get(method)
     * 									.invoke(fallbackFactory.create(ex), args);
     * 							return fallbackResult;
     *                        }
     * 						catch (IllegalAccessException e) {
     * 							// shouldn't happen as method is public due to being an
     * 							// interface
     * 							throw new AssertionError(e);
     *                        }
     * 						catch (InvocationTargetException e) {
     * 					        // 此处如果调用方法失败，将会包裹一层AssertionError,需要在GlobalTransaction里面捕获这个错误做回滚操作
     * 					        // 后续版本已经修改成获取异常抛出
     * 					        // <a href="https://github.com/alibaba/spring-cloud-alibaba/pull/3786/files#diff-a10a7e3094ac1a97299e2d51e0fc6425090434dcbc0644c360eee7877d354a33">fallback Method throw exception but seata does not rollback</a>
     * 							throw new AssertionError(e.getCause());
     *                        }
     *                    }
     * 					else {
     * 						// throw exception if fallbackFactory is null
     * 						throw ex;
     *                    }
     *                }
     * 				finally {
     * 					if (entry != null) {
     * 						entry.exit(1, args);
     *                    }
     * 					ContextUtil.exit();
     *                }
     *            }
     *        }
     * 		else {
     * 			// other target type using default strategy
     * 			result = methodHandler.invoke(args);
     *        }
     *
     * 		return result;
     *    }}
     * </pre>
     * @param id 用户id
     * @return
     * @see SentinelInvocationHandler#invoke(Object, Method, Object[])
     */
    @GlobalTransactional(rollbackFor = {Exception.class, RuntimeException.class, AssertionError.class})
    @Override
    public boolean createOrder(Integer id) {
        boolean b = userMapper.subBalance(id, BigDecimal.ONE);
        if (b) {

            TransactionHookManager.registerHook(new TransactionHookAdapter() {
                @Override
                public void afterCommit() {
                    System.out.println("successful -> commited");
                }

                @Override
                public void afterRollback() {
                    System.out.println("fail -> after rollback");
                }
            });
            int order = orderApi.createOrder(id);

//              本地事务才能使用
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                @Override
//                public void afterCompletion(int status) {
//                    System.out.println("status = " + status);
//                }
//            });
            return order == 1;
        } else {
            return false;
        }
    }


}
