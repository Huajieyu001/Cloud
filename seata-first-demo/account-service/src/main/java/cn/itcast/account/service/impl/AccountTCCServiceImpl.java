package cn.itcast.account.service.impl;

import cn.itcast.account.entity.Account;
import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.AccountService;
import cn.itcast.account.service.AccountTCCService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 虎哥
 */
@Slf4j
@Service
public class AccountTCCServiceImpl implements AccountTCCService {

    @Autowired
    private AccountFreezeMapper freezeMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public void deduct(String userId, int money) {
        AccountFreeze accountFreeze = freezeMapper.selectById(userId);
        // 如果存在冻结资源，说明已经尝试过try，停止执行后续操作
        if(accountFreeze != null){
            return ;
        }

        String xid = RootContext.getXID();
        AccountFreeze freeze = new AccountFreeze();
        freeze.setXid(xid);
        freeze.setUserId(userId);
        freeze.setState(AccountFreeze.State.TRY);
        freeze.setFreezeMoney(money);
        // 插入冻结资源
        freezeMapper.insert(freeze);

        // 扣除可用资源
        Account account = accountMapper.selectByUserId(userId);
        account.setMoney(account.getMoney() - money);
        accountMapper.updateById(account);
    }

    @Override
    public boolean confirm(BusinessActionContext context) {
        // 删除冻结资源
        return freezeMapper.deleteById(context.getXid()) == 1;
    }

    @Override
    public boolean cancel(BusinessActionContext context) {
        String xid = context.getXid();
        AccountFreeze accountFreeze = freezeMapper.selectById(context.getXid());

        // 如果事务没有冻结资源，说明还没有进行过try操作，if内部进行空回滚
        if(accountFreeze == null){
            AccountFreeze freeze = new AccountFreeze();
            freeze.setXid(xid);
            freeze.setUserId(context.getActionContext("userId").toString());
            freeze.setState(AccountFreeze.State.CANCEL);
            freeze.setFreezeMoney(0);
            return freezeMapper.insert(freeze) == 1;
        }
        if(accountFreeze.getState() == AccountFreeze.State.CANCEL){
            return true;
        }

        Account account = accountMapper.selectByUserId(accountFreeze.getUserId());

        // 恢复被冻结金额
        account.setMoney(account.getMoney() + accountFreeze.getFreezeMoney());
        accountMapper.updateById(account);

        // 清零冻结金额，并设置冻结事务的状态为cancel，避免业务悬挂
        accountFreeze.setFreezeMoney(0);
        accountFreeze.setState(AccountFreeze.State.CANCEL);
        return freezeMapper.updateById(accountFreeze) == 1;
    }
}
