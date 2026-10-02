package net.vivans.dcim.module.calculated.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.shared.persistence.BaseEntity;

import java.util.*;

@Entity
@Table(name = "calculated_metric", uniqueConstraints = @UniqueConstraint(name = "uk_calculated_metric_name", columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalculatedMetric extends BaseEntity {
    private static final String DEFAULT_CRON = "0 */5 * * * *";
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "calculation_cron", nullable = false, length = 64)
    private String calculationCron;
    @Column(name = "collection_enabled", nullable = false)
    private boolean collectionEnabled;
    @Column(name = "config_version", nullable = false)
    private int configVersion;
    @Column(name = "collector_job_id", length = 100)
    private String collectorJobId;
    @Column(name = "formula", nullable = false, length = 500)
    private String formula;
    @Column(name = "result_unit", length = 32)
    private String resultUnit;
    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private final Set<CalculatedMetricSource> sources = new LinkedHashSet<>();
    public static CalculatedMetric createCalculated(String name, String calculationCron, boolean collectionEnabled,
                                                  String formula, String resultUnit, List<CalculatedSourceDefinition> sources) {
        CalculatedMetric definition = new CalculatedMetric();
        definition.name = requireName(name);
        definition.calculationCron = normalizeCron(calculationCron);
        definition.collectionEnabled = collectionEnabled;
        definition.configVersion = 1;
        definition.resultUnit = resultUnit == null ? "" : resultUnit.trim();
        definition.replaceCalculatedSources(formula, sources);
        return definition;
    }
    public void updateCalculated(String name, String calculationCron, String formula, String resultUnit,
                                 List<CalculatedSourceDefinition> sourceDefinitions) {
        this.name = requireName(name);
        this.calculationCron = normalizeCron(calculationCron);
        this.resultUnit = resultUnit == null ? "" : resultUnit.trim();
        replaceCalculatedSources(formula, sourceDefinitions);
        this.configVersion++;
    }
    private void replaceCalculatedSources(String formula, List<CalculatedSourceDefinition> definitions) {
        if (formula == null || formula.isBlank() || formula.length() > 500) {
            throw new IllegalArgumentException("formula must contain 1-500 characters");
        }
        if (definitions == null || definitions.isEmpty() || definitions.size() > 32) {
            throw new IllegalArgumentException("formula requires 1-32 sources");
        }
        Set<String> aliases = new HashSet<>();
        for (CalculatedSourceDefinition source : definitions) {
            if (source == null || source.device() == null || source.device().getId() == null
                    || source.pointName() == null || source.pointName().isBlank()
                    || source.alias() == null || !source.alias().matches("[A-Za-z][A-Za-z0-9_]{0,31}")
                    || !("snmp".equals(source.protocol()) || "modbus".equals(source.protocol()))) {
                throw new IllegalArgumentException("invalid formula source");
            }
            if (!aliases.add(source.alias())) throw new IllegalArgumentException("duplicate formula alias: " + source.alias());
        }
        Set<String> references = formulaReferences(formula);
        if (!references.equals(aliases)) {
            throw new IllegalArgumentException("formula must reference exactly the configured aliases");
        }
        this.formula = formula.trim();
        Map<String, CalculatedMetricSource> existing = new HashMap<>();
        for (CalculatedMetricSource source : sources) if (source.getAlias() != null) existing.put(source.getAlias(), source);
        for (CalculatedSourceDefinition source : definitions) {
            CalculatedMetricSource current = existing.get(source.alias());
            if (current == null) sources.add(CalculatedMetricSource.createCalculated(this, source.device(), source.alias(), source.pointName(), source.protocol()));
            else current.updateCalculated(source.device(), source.pointName(), source.protocol());
        }
        sources.removeIf(source -> !aliases.contains(source.getAlias()));
    }
    public List<CalculatedSourceDefinition> calculatedSources() {
        return sources.stream().filter(source -> source.getAlias() != null)
                .map(source -> new CalculatedSourceDefinition(source.getDevice(), source.getAlias(), source.getPointName(), source.getProtocol()))
                .toList();
    }
    private static Set<String> formulaReferences(String formula) {
        Set<String> references = new HashSet<>();
        int depth = 0;
        boolean operand = true;
        for (int index = 0; index < formula.length();) {
            char current = formula.charAt(index);
            if (Character.isWhitespace(current)) { index++; continue; }
            if (operand) {
                if (current == '(') { depth++; index++; continue; }
                if (current == '+' || current == '-') { index++; continue; }
                if (Character.isLetter(current)) {
                    int start = index++;
                    while (index < formula.length() && (Character.isLetterOrDigit(formula.charAt(index))
                            || formula.charAt(index) == '_')) index++;
                    references.add(formula.substring(start, index));
                    operand = false;
                    continue;
                }
                if (Character.isDigit(current) || current == '.') {
                    int digits = 0;
                    while (index < formula.length() && Character.isDigit(formula.charAt(index))) { index++; digits++; }
                    if (index < formula.length() && formula.charAt(index) == '.') {
                        index++;
                        while (index < formula.length() && Character.isDigit(formula.charAt(index))) { index++; digits++; }
                    }
                    if (digits == 0) throw new IllegalArgumentException("invalid formula number");
                    operand = false;
                    continue;
                }
            } else {
                if (current == ')' && depth > 0) { depth--; index++; continue; }
                if (current == '+' || current == '-' || current == '*' || current == '/') {
                    operand = true; index++; continue;
                }
            }
            throw new IllegalArgumentException("invalid formula at position " + index);
        }
        if (operand || depth != 0) throw new IllegalArgumentException("incomplete formula");
        return references;
    }
    public void setCollectionEnabled(boolean collectionEnabled) { this.collectionEnabled = collectionEnabled; }
    public void updateCollectorJobId(String collectorJobId) { this.collectorJobId = collectorJobId; }
    private static String requireName(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("name is required"); return value.trim(); }
    private static String normalizeCron(String value) { return value == null || value.isBlank() ? DEFAULT_CRON : value.trim(); }
    public record CalculatedSourceDefinition(Device device, String alias, String pointName, String protocol) {}
}
