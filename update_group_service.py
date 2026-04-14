import re

with open("wecom-scrm-server/src/main/java/com/wecom/scrm/service/GroupMessageService.java", "r") as f:
    content = f.read()

# 1. Update findMatchedChatIds to support singleOwner
content = content.replace(
    "List<String> findMatchedChatIds(WecomGroupMessage message) throws Exception {",
    "List<String> findMatchedChatIds(WecomGroupMessage message) throws Exception {\n        return findMatchedChatIds(message, null);\n    }\n\n    @SuppressWarnings(\"unchecked\")\n    List<String> findMatchedChatIds(WecomGroupMessage message, String singleOwner) throws Exception {"
)

content = content.replace(
    "        if (condition != null) {",
    "        if (singleOwner != null) {\n            wheres.add(\"g.owner = :singleOwner\");\n        }\n\n        if (condition != null) {"
)

content = content.replace(
    "        if (condition != null) {",
    "        if (singleOwner != null) {\n            query.setParameter(\"singleOwner\", singleOwner);\n        }\n\n        if (condition != null) {",
    1 # Only replace the first occurrence (binding part)
)

# 2. Add findMatchedOwners method
find_owners_method = """
    @SuppressWarnings("unchecked")
    List<String> findMatchedOwners(WecomGroupMessage message) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT g.owner FROM wecom_group_chat g");

        List<String> wheres = new ArrayList<>();
        wheres.add("g.status = 0");

        GroupMessageDTO.TargetCondition condition = null;
        if (message.getTargetType() == 1 && message.getTargetCondition() != null) {
            condition = objectMapper.readValue(message.getTargetCondition(), GroupMessageDTO.TargetCondition.class);
        }

        if (condition != null) {
            if (condition.getCreateTimeStart() != null) wheres.add("g.create_time >= :createTimeStart");
            if (condition.getCreateTimeEnd() != null) wheres.add("g.create_time <= :createTimeEnd");
            if (condition.getGroupNameKeywords() != null && !condition.getGroupNameKeywords().isEmpty()) {
                List<String> keywordConditions = new ArrayList<>();
                for (int i = 0; i < condition.getGroupNameKeywords().size(); i++) {
                    keywordConditions.add("g.name LIKE :keyword" + i);
                }
                wheres.add("(" + String.join(" OR ", keywordConditions) + ")");
            }
            if (condition.getOwnerUserids() != null && !condition.getOwnerUserids().isEmpty()) {
                wheres.add("g.owner IN (:ownerUserids)");
            }
        }

        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }

        Query query = entityManager.createNativeQuery(sql.toString());
        if (condition != null) {
            if (condition.getCreateTimeStart() != null) query.setParameter("createTimeStart", condition.getCreateTimeStart());
            if (condition.getCreateTimeEnd() != null) query.setParameter("createTimeEnd", condition.getCreateTimeEnd());
            if (condition.getGroupNameKeywords() != null && !condition.getGroupNameKeywords().isEmpty()) {
                for (int i = 0; i < condition.getGroupNameKeywords().size(); i++) {
                    query.setParameter("keyword" + i, "%" + condition.getGroupNameKeywords().get(i) + "%");
                }
            }
            if (condition.getOwnerUserids() != null && !condition.getOwnerUserids().isEmpty()) {
                query.setParameter("ownerUserids", condition.getOwnerUserids());
            }
        }
        return (List<String>) query.getResultList();
    }
"""

content = content.replace("    public List<WecomGroupMessage> listTasks() {", find_owners_method + "\n    public List<WecomGroupMessage> listTasks() {")

with open("wecom-scrm-server/src/main/java/com/wecom/scrm/service/GroupMessageService.java", "w") as f:
    f.write(content)
