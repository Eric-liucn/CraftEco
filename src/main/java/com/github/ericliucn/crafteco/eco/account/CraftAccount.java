package com.github.ericliucn.crafteco.eco.account;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.leangen.geantyref.TypeToken;
import javafx.util.Pair;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CraftAccount implements Account, UniqueAccount {

    private Map<Currency, BigDecimal> balanceMap;
    private final UUID uniqueID;
    protected boolean isVirtual;

    public CraftAccount(final UUID uniqueID){
        this.balanceMap = new ConcurrentHashMap<>();
        this.uniqueID = uniqueID;
        this.isVirtual = false;
    }

    @Override
    public Component displayName() {
        return Util.toComponent(this.identifier());
    }

    @Override
    public BigDecimal defaultBalance(Currency currency) {
        return currency instanceof CraftCurrency ? ((CraftCurrency) currency).defaultValue():BigDecimal.ZERO;
    }

    @Override
    public boolean hasBalance(Currency currency, Set<Context> contexts) {
        return balanceMap.containsKey(currency);
    }

    @Override
    public boolean hasBalance(Currency currency, Cause cause) {
        return balanceMap.containsKey(currency);
    }

    @Override
    public BigDecimal balance(Currency currency, Set<Context> contexts) {
        return balanceMap.getOrDefault(currency, defaultBalance(currency));
    }

    @Override
    public BigDecimal balance(Currency currency, Cause cause) {
        return balanceMap.getOrDefault(currency, defaultBalance(currency));
    }

    @Override
    public Map<Currency, BigDecimal> balances(Set<Context> contexts) {
        return balances();
    }

    @Override
    public Map<Currency, BigDecimal> balances(Cause cause) {
        return balances();
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, amount);
            return CraftResult.builder().currency((CraftCurrency) currency).account(this).amount(amount).result(ResultType.SUCCESS).build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult) setBalance(currency, amount)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult) setBalance(currency, amount)).toBuilder().cause(cause).build();
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances() {
        final Map<Currency, TransactionResult> resultMap = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            CraftCurrency craftCurrency = (CraftCurrency) entry.getKey();
            resultMap.put(craftCurrency, setBalance(craftCurrency, defaultBalance(craftCurrency)));
        }
        return resultMap;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        Map<Currency, TransactionResult> map = resetBalances();
        map.replaceAll((k, v) -> ((CraftResult) v).toBuilder().contexts(contexts).build());
        return map;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        Map<Currency, TransactionResult> map = resetBalances();
        map.replaceAll((k, v) -> ((CraftResult) v).toBuilder().cause(cause).build());
        return map;
    }

    @Override
    public TransactionResult resetBalance(Currency currency) {
        return setBalance(currency, defaultBalance(currency));
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Set<Context> contexts) {
        return ((CraftResult) resetBalance(currency)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause) {
        return ((CraftResult) resetBalance(currency)).toBuilder().cause(cause).build();
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, balance(currency).add(amount));
            return CraftResult.builder()
                    .currency((CraftCurrency) currency)
                    .account(this)
                    .result(ResultType.SUCCESS)
                    .type(TransactionTypes.DEPOSIT.get())
                    .amount(amount)
                    .build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult) deposit(currency, amount)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult) deposit(currency, amount)).toBuilder().cause(cause).build();
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, balance(currency).subtract(amount));
            return CraftResult.builder()
                    .currency((CraftCurrency) currency)
                    .account(this)
                    .result(ResultType.SUCCESS)
                    .type(TransactionTypes.WITHDRAW.get())
                    .amount(amount)
                    .build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult) withdraw(currency, amount)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult) withdraw(currency, amount)).toBuilder().cause(cause).build();
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount) {
        if ((to instanceof CraftAccount) && (currency instanceof CraftCurrency)){
            CraftResult wResult = (CraftResult) withdraw(currency, amount);
            CraftResult dResult = (CraftResult) to.deposit(currency, amount);
            if (wResult.result().equals(ResultType.SUCCESS) && dResult.result().equals(ResultType.SUCCESS)){
                return wResult.toBuilder().type(TransactionTypes.TRANSFER.get()).build().toTransferResult(((CraftAccount) to));
            }else {
                throw new IllegalStateException("Transfer failed!");
            }
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult.CraftTransferResult) transfer(to, currency, amount)).toBuilder().contexts(contexts).build().toTransferResult(((CraftAccount) to));
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult.CraftTransferResult) transfer(to, currency, amount)).toBuilder().cause(cause).build().toTransferResult(((CraftAccount) to));
    }


    @Override
    public String identifier() {
        return this.uniqueID.toString();
    }

    public boolean isVirtual(){
        return this.isVirtual;
    }

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, Map<String, String>>>(){}.getType();

    public byte[] serialize() throws IOException {
        Map<String , Map<String, String>> data = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
        Map<String,String> balanceData = new HashMap<>();
        properties.put("uuid", this.uniqueID.toString());
        properties.put("identifier", this.identifier());
        properties.put("isVirtual", this.isVirtual ? "true":"false");
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            CraftCurrency currency = ((CraftCurrency) entry.getKey());
            balanceData.put(currency.toPlain(), entry.getValue().toString());
        }
        data.put("properties", properties);
        data.put("balanceData", balanceData);
        return GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    @Nullable
    public static CraftAccount deserialize(byte[] bytes) {
        try {
            String dataStr = new String(bytes, StandardCharsets.UTF_8);
            Map<String , Map<String, String>> data = GSON.fromJson(dataStr, TYPE);
            Map<String, String> properties = data.get("properties");
            Map<String,String> balanceData = data.get("balanceData");
            UUID uuid = UUID.fromString(properties.get("uuid"));
            String identifier = properties.get("identifier");
            boolean isVirtual = properties.get("isVirtual").equals("true");
            CraftAccount account = isVirtual ? new CraftVirtualAccount(identifier, uuid) : new CraftAccount(uuid);
            Map<Currency, BigDecimal> balMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> entry : balanceData.entrySet()) {
                for (CraftCurrency currency : ConfigLoader.instance.getConfig().currencies) {
                    if (entry.getKey().equals(currency.toPlain())){
                        balMap.put(currency, new BigDecimal(entry.getValue()));
                    }
                }
            }
            account.balanceMap = balMap;
            return account;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UUID uniqueId() {
        return this.uniqueID;
    }
}
