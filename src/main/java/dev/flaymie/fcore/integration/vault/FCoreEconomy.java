package dev.flaymie.fcore.integration.vault;

import dev.flaymie.fcore.FCore;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Адаптер экономики FCore для Vault API
 */
public class FCoreEconomy implements Economy {
    
    private final FCore plugin;
    private final Logger logger;
    private boolean enabled = false;
    private String currencyNameSingular = "монета";
    private String currencyNamePlural = "монет";

    public FCoreEconomy(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Включает экономику
     */
    public void enable() {
        enabled = true;
        logger.info("Экономика FCore через Vault включена");
    }

    /**
     * Отключает экономику
     */
    public void disable() {
        enabled = false;
        logger.info("Экономика FCore через Vault отключена");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "FCoreEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false; // FCore пока не поддерживает банки
    }

    @Override
    public int fractionalDigits() {
        return 2; // Поддерживаем 2 знака после запятой
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f %s", amount, amount == 1 ? currencyNameSingular : currencyNamePlural);
    }

    @Override
    public String currencyNamePlural() {
        return currencyNamePlural;
    }

    @Override
    public String currencyNameSingular() {
        return currencyNameSingular;
    }

    @Override
    public boolean hasAccount(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        return player != null && hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        // Проверяем наличие аккаунта по UUID игрока
        // В реальной реализации здесь будет обращение к БД FCore
        // Временная реализация возвращает true для всех существующих игроков
        return player != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName); // Игнорируем имя мира
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player); // Игнорируем имя мира
    }

    @Override
    public double getBalance(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        return player != null ? getBalance(player) : 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        // В реальной реализации здесь будет получение баланса из БД FCore
        // Временная реализация возвращает 100 для всех игроков
        return 100.0;
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName); // Игнорируем имя мира
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player); // Игнорируем имя мира
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount); // Игнорируем имя мира
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount); // Игнорируем имя мира
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Игрок не найден");
        }
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (!hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "У игрока нет аккаунта");
        }

        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Сумма должна быть положительной");
        }

        double balance = getBalance(player);
        if (balance < amount) {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Недостаточно средств");
        }

        // В реальной реализации здесь будет списание средств в БД FCore
        double newBalance = balance - amount;
        
        // Логирование операции
        logger.info("Списание " + amount + " с баланса игрока " + player.getName() + ". Новый баланс: " + newBalance);
        
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount); // Игнорируем имя мира
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount); // Игнорируем имя мира
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Игрок не найден");
        }
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (!hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "У игрока нет аккаунта");
        }

        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Сумма должна быть положительной");
        }

        // В реальной реализации здесь будет пополнение баланса в БД FCore
        double newBalance = getBalance(player) + amount;
        
        // Логирование операции
        logger.info("Пополнение " + amount + " на баланс игрока " + player.getName() + ". Новый баланс: " + newBalance);
        
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount); // Игнорируем имя мира
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount); // Игнорируем имя мира
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "FCore не поддерживает банки");
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>(); // Пустой список, так как банки не поддерживаются
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true; // Аккаунт создается автоматически
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // Аккаунт создается автоматически
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName); // Игнорируем имя мира
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player); // Игнорируем имя мира
    }
} 