import re

with open("wecom-scrm-server/src/main/java/com/wecom/scrm/service/CustomerMessageService.java", "r") as f:
    content = f.read()

# Replace findMatchedExternalUserids signature
content = content.replace("List<String> findMatchedExternalUserids(WecomCustomerMessage message) throws Exception {", "List<String> findMatchedExternalUserids(WecomCustomerMessage message) throws Exception {\n        return findMatchedExternalUserids(message, null);\n    }\n\n    @SuppressWarnings(\"unchecked\")\n    List<String> findMatchedExternalUserids(WecomCustomerMessage message, String singleSender) throws Exception {")

content = content.replace("        if (message.getSenderList() != null) {\n            List<String> senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {\n            });\n            if (!senders.isEmpty()) {\n                wheres.add(\"r.userid IN (:senders)\");\n            }\n        }", "        if (singleSender != null) {\n            wheres.add(\"r.userid = :singleSender\");\n        } else if (message.getSenderList() != null) {\n            List<String> senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {\n            });\n            if (!senders.isEmpty()) {\n                wheres.add(\"r.userid IN (:senders)\");\n            }\n        }")

content = content.replace("        if (message.getSenderList() != null) {\n            List<String> senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {\n            });\n            if (!senders.isEmpty()) {\n                query.setParameter(\"senders\", senders);\n            }\n        }", "        if (singleSender != null) {\n            query.setParameter(\"singleSender\", singleSender);\n        } else if (message.getSenderList() != null) {\n            List<String> senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {\n            });\n            if (!senders.isEmpty()) {\n                query.setParameter(\"senders\", senders);\n            }\n        }")

with open("wecom-scrm-server/src/main/java/com/wecom/scrm/service/CustomerMessageService.java", "w") as f:
    f.write(content)
