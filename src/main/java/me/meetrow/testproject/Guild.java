package me.meetrow.testproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Guild {
    private static final int MAX_LOG_ENTRIES = 80;

    private final String id;
    private String name;
    private String tag;
    private UUID leaderId;
    private double balance;
    private int cachedLevel;
    private int cachedScore;
    private int guildXp;
    private String description;
    private String motd;
    private boolean recruitingOpen;
    private long createdAtMillis;
    private String capitalCountryKey;
    private final Set<UUID> members;
    private final Map<UUID, Long> invitedPlayers;
    private final Map<UUID, UUID> inviteSenders;
    private final Set<String> claimedCountryKeys;
    private final Map<UUID, GuildRole> memberRoles;
    private final Map<GuildRole, Map<GuildPermission, GuildPermissionState>> rolePermissionOverrides;
    private final Map<UUID, Map<GuildPermission, GuildPermissionState>> playerPermissionOverrides;
    private final Map<String, Integer> stockpile;
    private final List<String> logs;

    public Guild(String id,
                 String name,
                 String tag,
                 UUID leaderId,
                 double balance,
                 int cachedLevel,
                 int cachedScore,
                 Set<UUID> members,
                 Set<UUID> invitedPlayers,
                 Set<String> claimedCountryKeys,
                 Map<UUID, GuildRole> memberRoles,
                 Map<GuildRole, Map<GuildPermission, GuildPermissionState>> rolePermissionOverrides,
                 Map<UUID, Map<GuildPermission, GuildPermissionState>> playerPermissionOverrides) {
        this(id, name, tag, leaderId, balance, cachedLevel, cachedScore, 0, "", "",
                true, System.currentTimeMillis(), null, members, convertInvites(invitedPlayers), Map.of(),
                claimedCountryKeys, memberRoles, rolePermissionOverrides, playerPermissionOverrides,
                Map.of(), List.of());
    }

    public Guild(String id,
                 String name,
                 String tag,
                 UUID leaderId,
                 double balance,
                 int cachedLevel,
                 int cachedScore,
                 int guildXp,
                 String description,
                 String motd,
                 boolean recruitingOpen,
                 long createdAtMillis,
                 String capitalCountryKey,
                 Set<UUID> members,
                 Map<UUID, Long> invitedPlayers,
                 Map<UUID, UUID> inviteSenders,
                 Set<String> claimedCountryKeys,
                 Map<UUID, GuildRole> memberRoles,
                 Map<GuildRole, Map<GuildPermission, GuildPermissionState>> rolePermissionOverrides,
                 Map<UUID, Map<GuildPermission, GuildPermissionState>> playerPermissionOverrides,
                 Map<String, Integer> stockpile,
                 List<String> logs) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leaderId = leaderId;
        this.balance = balance;
        this.cachedLevel = Math.max(1, cachedLevel);
        this.cachedScore = Math.max(0, cachedScore);
        this.guildXp = Math.max(0, guildXp);
        this.description = description == null ? "" : description;
        this.motd = motd == null ? "" : motd;
        this.recruitingOpen = recruitingOpen;
        this.createdAtMillis = Math.max(0L, createdAtMillis);
        this.capitalCountryKey = capitalCountryKey;
        this.members = new LinkedHashSet<>(members);
        this.invitedPlayers = new LinkedHashMap<>(sanitizeInvites(invitedPlayers));
        this.inviteSenders = new LinkedHashMap<>(sanitizeInviteSenders(inviteSenders));
        this.claimedCountryKeys = new LinkedHashSet<>(claimedCountryKeys);
        this.memberRoles = new LinkedHashMap<>(memberRoles);
        this.rolePermissionOverrides = copyRolePermissionOverrides(rolePermissionOverrides);
        this.playerPermissionOverrides = copyPlayerPermissionOverrides(playerPermissionOverrides);
        this.stockpile = copyStockpile(stockpile);
        this.logs = new ArrayList<>(logs != null ? logs : List.of());
        trimLogs();
        if (leaderId != null) {
            this.members.add(leaderId);
        }
    }

    private static Map<UUID, Long> convertInvites(Set<UUID> invitedPlayers) {
        Map<UUID, Long> converted = new LinkedHashMap<>();
        if (invitedPlayers == null) {
            return converted;
        }
        long now = System.currentTimeMillis();
        for (UUID inviteId : invitedPlayers) {
            if (inviteId != null) {
                converted.put(inviteId, now);
            }
        }
        return converted;
    }

    private Map<GuildRole, Map<GuildPermission, GuildPermissionState>> copyRolePermissionOverrides(Map<GuildRole, Map<GuildPermission, GuildPermissionState>> input) {
        Map<GuildRole, Map<GuildPermission, GuildPermissionState>> copy = new EnumMap<>(GuildRole.class);
        if (input == null) {
            return copy;
        }
        for (Map.Entry<GuildRole, Map<GuildPermission, GuildPermissionState>> entry : input.entrySet()) {
            Map<GuildPermission, GuildPermissionState> overrides = new EnumMap<>(GuildPermission.class);
            if (entry.getValue() != null) {
                overrides.putAll(entry.getValue());
            }
            copy.put(entry.getKey(), overrides);
        }
        return copy;
    }

    private Map<UUID, Map<GuildPermission, GuildPermissionState>> copyPlayerPermissionOverrides(Map<UUID, Map<GuildPermission, GuildPermissionState>> input) {
        Map<UUID, Map<GuildPermission, GuildPermissionState>> copy = new LinkedHashMap<>();
        if (input == null) {
            return copy;
        }
        for (Map.Entry<UUID, Map<GuildPermission, GuildPermissionState>> entry : input.entrySet()) {
            Map<GuildPermission, GuildPermissionState> overrides = new EnumMap<>(GuildPermission.class);
            if (entry.getValue() != null) {
                overrides.putAll(entry.getValue());
            }
            copy.put(entry.getKey(), overrides);
        }
        return copy;
    }

    private Map<String, Integer> copyStockpile(Map<String, Integer> input) {
        Map<String, Integer> copy = new LinkedHashMap<>();
        if (input == null) {
            return copy;
        }
        for (Map.Entry<String, Integer> entry : input.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            copy.put(entry.getKey(), Math.max(0, entry.getValue() != null ? entry.getValue() : 0));
        }
        return copy;
    }

    private Map<UUID, Long> sanitizeInvites(Map<UUID, Long> input) {
        Map<UUID, Long> sanitized = new LinkedHashMap<>();
        if (input == null) {
            return sanitized;
        }
        for (Map.Entry<UUID, Long> entry : input.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            sanitized.put(entry.getKey(), Math.max(0L, entry.getValue() != null ? entry.getValue() : 0L));
        }
        return sanitized;
    }

    private Map<UUID, UUID> sanitizeInviteSenders(Map<UUID, UUID> input) {
        Map<UUID, UUID> sanitized = new LinkedHashMap<>();
        if (input == null) {
            return sanitized;
        }
        for (Map.Entry<UUID, UUID> entry : input.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            sanitized.put(entry.getKey(), entry.getValue());
        }
        return sanitized;
    }

    private void trimLogs() {
        while (logs.size() > MAX_LOG_ENTRIES) {
            logs.remove(0);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
        if (leaderId != null) {
            members.add(leaderId);
        }
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getCachedLevel() {
        return cachedLevel;
    }

    public void setCachedLevel(int cachedLevel) {
        this.cachedLevel = Math.max(1, cachedLevel);
    }

    public int getCachedScore() {
        return cachedScore;
    }

    public void setCachedScore(int cachedScore) {
        this.cachedScore = Math.max(0, cachedScore);
    }

    public int getGuildXp() {
        return guildXp;
    }

    public void setGuildXp(int guildXp) {
        this.guildXp = Math.max(0, guildXp);
    }

    public void addGuildXp(int amount) {
        this.guildXp = Math.max(0, this.guildXp + Math.max(0, amount));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd == null ? "" : motd;
    }

    public boolean isRecruitingOpen() {
        return recruitingOpen;
    }

    public void setRecruitingOpen(boolean recruitingOpen) {
        this.recruitingOpen = recruitingOpen;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = Math.max(0L, createdAtMillis);
    }

    public String getCapitalCountryKey() {
        return capitalCountryKey;
    }

    public void setCapitalCountryKey(String capitalCountryKey) {
        this.capitalCountryKey = capitalCountryKey;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getInvitedPlayers() {
        return Collections.unmodifiableSet(invitedPlayers.keySet());
    }

    public Map<UUID, Long> getInviteExpiries() {
        return invitedPlayers;
    }

    public Map<UUID, UUID> getInviteSenders() {
        return inviteSenders;
    }

    public long getInviteExpiry(UUID playerId) {
        return playerId != null ? invitedPlayers.getOrDefault(playerId, 0L) : 0L;
    }

    public UUID getInviteSender(UUID playerId) {
        return playerId != null ? inviteSenders.get(playerId) : null;
    }

    public void setInviteExpiry(UUID playerId, long expiryMillis) {
        if (playerId == null) {
            return;
        }
        invitedPlayers.put(playerId, Math.max(0L, expiryMillis));
    }

    public void setInvite(UUID playerId, long expiryMillis, UUID inviterId) {
        if (playerId == null) {
            return;
        }
        invitedPlayers.put(playerId, Math.max(0L, expiryMillis));
        if (inviterId == null) {
            inviteSenders.remove(playerId);
        } else {
            inviteSenders.put(playerId, inviterId);
        }
    }

    public void removeInvite(UUID playerId) {
        if (playerId == null) {
            return;
        }
        invitedPlayers.remove(playerId);
        inviteSenders.remove(playerId);
    }

    public Set<String> getClaimedCountryKeys() {
        return claimedCountryKeys;
    }

    public Map<UUID, GuildRole> getMemberRoles() {
        return memberRoles;
    }

    public Map<GuildRole, Map<GuildPermission, GuildPermissionState>> getRolePermissionOverrides() {
        return rolePermissionOverrides;
    }

    public Map<UUID, Map<GuildPermission, GuildPermissionState>> getPlayerPermissionOverrides() {
        return playerPermissionOverrides;
    }

    public Map<String, Integer> getStockpile() {
        return stockpile;
    }

    public int getStockpileAmount(String materialKey) {
        if (materialKey == null || materialKey.isBlank()) {
            return 0;
        }
        return Math.max(0, stockpile.getOrDefault(materialKey, 0));
    }

    public void addStockpile(String materialKey, int amount) {
        if (materialKey == null || materialKey.isBlank() || amount <= 0) {
            return;
        }
        stockpile.put(materialKey, Math.max(0, stockpile.getOrDefault(materialKey, 0) + amount));
    }

    public List<String> getLogs() {
        return logs;
    }

    public void addLog(String logLine) {
        if (logLine == null || logLine.isBlank()) {
            return;
        }
        logs.add(logLine);
        trimLogs();
    }

    public GuildRole getRole(UUID playerId) {
        if (playerId == null || !members.contains(playerId)) {
            return null;
        }
        if (playerId.equals(leaderId)) {
            return GuildRole.LEADER;
        }
        return memberRoles.getOrDefault(playerId, GuildRole.MEMBER);
    }

    public void setRole(UUID playerId, GuildRole role) {
        if (playerId == null) {
            return;
        }
        if (playerId.equals(leaderId) || role == GuildRole.LEADER) {
            return;
        }
        members.add(playerId);
        if (role == null || role == GuildRole.MEMBER) {
            memberRoles.remove(playerId);
            return;
        }
        memberRoles.put(playerId, role);
    }

    public void removeMember(UUID playerId) {
        if (playerId == null) {
            return;
        }
        members.remove(playerId);
        invitedPlayers.remove(playerId);
        inviteSenders.remove(playerId);
        memberRoles.remove(playerId);
        playerPermissionOverrides.remove(playerId);
    }

    public List<UUID> getMembersSortedByRole() {
        List<UUID> ordered = new ArrayList<>(members);
        ordered.sort((left, right) -> {
            GuildRole leftRole = getRole(left);
            GuildRole rightRole = getRole(right);
            int comparison = Integer.compare(leftRole != null ? leftRole.ordinal() : Integer.MAX_VALUE, rightRole != null ? rightRole.ordinal() : Integer.MAX_VALUE);
            if (comparison != 0) {
                return comparison;
            }
            return left.toString().compareToIgnoreCase(right.toString());
        });
        return ordered;
    }
}
