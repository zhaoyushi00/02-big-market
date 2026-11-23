// 声明当前类所属的包路径：基础设施层 - 持久化 - DAO
package com.zhaoyushi.infrastructure.persistent.dao;

// 引入用户积分账户持久化对象，作为 DAO 方法的入参类型
import com.zhaoyushi.infrastructure.persistent.po.UserCreditAccount;
// 引入 MyBatis 的 @Mapper 注解，将本接口注册为 MyBatis Mapper
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分账户 DAO 接口（基础设施层 - 持久化）
 *
 * <p>定义用户积分账户表（user_credit_account）的持久化操作，
 * 由 MyBatis 通过同名 XML（user_credit_account_mapper.xml）提供 SQL 实现。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 用户积分账户
 */
// 将本接口注册为 MyBatis Mapper，由 MyBatis 扫描并生成代理实现
@Mapper
public interface IUserCreditAccountDao {

    /**
     * 插入用户积分账户记录（首次发奖时创建账户）
     *
     * @param userCreditAccountReq 用户积分账户持久化对象
     */
    public void insert(UserCreditAccount userCreditAccountReq);

    /**
     * 累加用户积分（总积分与可用积分同时累加）
     *
     * @param userCreditAccountReq 用户积分账户持久化对象（携带累加值）
     * @return 受影响的行数（为 0 表示账户不存在，需要新建）
     */
    public int updateAddAmount(UserCreditAccount userCreditAccountReq);

}
