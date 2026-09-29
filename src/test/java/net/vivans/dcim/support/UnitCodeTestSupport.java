package net.vivans.dcim.support;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;

public final class UnitCodeTestSupport {

    private UnitCodeTestSupport() {
    }

    public static Integer unitCodeId(String symbol, CodeGroupRepository groups, CommonCodeRepository codes) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        String code = switch (symbol) {
            case "C", "°C" -> "CELSIUS";
            case "%" -> "PERCENT";
            case "L/min" -> "L_PER_MIN";
            case "kWh", "kwh" -> "KWH";
            case "w" -> "W";
            case "W", "V", "A" -> symbol;
            default -> throw new IllegalArgumentException("Unsupported test unit: " + symbol);
        };
        CodeGroup unitGroup = groups.findAll().stream()
                .filter(group -> "UNIT".equals(group.getGroupKey()))
                .findFirst()
                .orElseGet(() -> groups.save(CodeGroup.createCodeGroup("UNIT", "Unit")));
        String displayName = switch (code) {
            case "CELSIUS" -> "°C";
            case "KWH" -> "kWh";
            case "W" -> "W";
            default -> symbol;
        };
        CommonCode unitCode = codes.findByCodeGroupGroupKeyAndCode("UNIT", code)
                .orElseGet(() -> codes.save(CommonCode.createCommonCode(unitGroup, code, displayName, 1)));
        return unitCode.getId();
    }
}
