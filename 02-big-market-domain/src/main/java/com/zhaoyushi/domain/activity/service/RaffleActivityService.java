package com.zhaoyushi.domain.activity.service;

import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import org.springframework.stereotype.Service;

/**
 * @author zhaoy
 * @date 2026/10/24
 * @description
 */
@Service
public class RaffleActivityService extends AbstractRaffleActivity{
    /**
     * 构造器注入活动仓储
     *
     * @param activityRepository 活动仓储实现
     */
    public RaffleActivityService(IActivityRepository activityRepository) {
        super(activityRepository);
    }
}
