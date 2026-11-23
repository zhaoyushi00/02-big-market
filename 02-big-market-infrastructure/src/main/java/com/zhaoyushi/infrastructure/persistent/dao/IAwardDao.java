package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*奖品表Dao*/
@Mapper
public interface IAwardDao {

    /**
     * 查询奖品列表
     *
     * @return 奖品列表
     */
    List<Award> queryAwardList();


    /**
     * 根据奖品ID查询奖品配置信息（如积分范围）
     *
     * @param awardId 奖品ID
     * @return 奖品配置字符串
     */
    String queryAwardConfigByAwardId(Integer awardId);

    /**
     * 根据奖品ID查询奖品类型标识（award_key，用于路由发奖策略）
     *
     * @param awardId 奖品ID
     * @return 奖品类型标识
     */
    String queryAwardKeyByAwardId(Integer awardId);
}
