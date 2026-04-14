package com.wecom.scrm.service;

import com.wecom.scrm.repository.*;
import com.wecom.scrm.vo.DashboardVO;
import com.wecom.scrm.dto.AddWayStatDTO;
import com.wecom.scrm.dto.TrendStatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WecomCustomerRepository wecomCustomerRepository;
    private final WecomCustomerRelationRepository wecomCustomerRelationRepository;
    private final WecomUserRepository wecomUserRepository;
    private final GroupChatRepository groupChatRepository;
    private final WecomCustomerMessageRepository wecomCustomerMessageRepository;

    public DashboardVO getSummary() {
        DashboardVO vo = new DashboardVO();

        vo.setTotalCustomerCount(wecomCustomerRepository.count());
        vo.setTotalEmployeeCount(wecomUserRepository.count());
        vo.setTotalGroupChatCount(groupChatRepository.count());
        vo.setTotalMessageCount(wecomCustomerMessageRepository.count());

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayNewCustomerCount(wecomCustomerRelationRepository.countByCreateTimeAfter(todayStart));

        // Trend for last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        List<TrendStatDTO> trendRaw = wecomCustomerRelationRepository.countTrendByCreateTimeAfter(sevenDaysAgo);
        vo.setCustomerGrowthTrend(trendRaw.stream().map(dto -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", dto.getDateString());
            map.put("count", dto.getCount());
            return map;
        }).collect(Collectors.toList()));

        // Add way distribution
        List<AddWayStatDTO> addWayRaw = wecomCustomerRelationRepository.countByAddWay();
        vo.setAddWayDistribution(addWayRaw.stream().map(dto -> {
            Map<String, Object> map = new HashMap<>();
            Integer way = dto.getAddWay();
            map.put("name", getAddWayName(way));
            map.put("value", dto.getCount());
            return map;
        }).collect(Collectors.toList()));

        return vo;
    }

    private String getAddWayName(Integer way) {
        if (way == null)
            return "未知";
        switch (way) {
            case 0:
                return "未知来源";
            case 1:
                return "扫描二维码";
            case 2:
                return "搜索手机号";
            case 3:
                return "名片分享";
            case 4:
                return "群聊";
            case 5:
                return "手机通讯录";
            case 6:
                return "微信联系人";
            case 8:
                return "安装第三方应用自动添加";
            case 9:
                return "搜索微信号";
            case 10:
                return "视频号添加";
            case 16:
                return "获客链接添加";
            case 201:
                return "内部成员共享";
            case 202:
                return "管理员/负责人分配";
            default:
                return "其他途径(" + way + ")";
        }
    }
}
