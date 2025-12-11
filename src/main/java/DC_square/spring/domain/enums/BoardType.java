package DC_square.spring.domain.enums;

public enum BoardType {
  자유게시판("자유 게시판"),
  정보공유게시판("정보 공유 게시판"),
  질문상담게시판("질문/상담 게시판"),
  입양임보게시판("입양/임보 게시판"),
  실종목격게시판("실종/목격 게시판");

  private final String displayName;

  BoardType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
