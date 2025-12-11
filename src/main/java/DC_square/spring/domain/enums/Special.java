package DC_square.spring.domain.enums;

public enum Special {
  TOILET, PARKING, WASTEBASKET, STAIRS, WATER, OTHER;

  public String getType() {
    return this.name();
  }

  public static Special fromString(String value) {
    for (Special special : Special.values()) {
      if (special.name().equalsIgnoreCase(value)) {
        return special;
      }
    }
    return OTHER;
  }
}