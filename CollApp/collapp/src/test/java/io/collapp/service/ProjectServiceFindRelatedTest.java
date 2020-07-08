
package io.collapp.service;

import io.collapp.config.PersistenceAndServiceConfig;
import io.collapp.model.*;
import io.collapp.model.BoardColumn.BoardColumnLocation;
import io.collapp.model.CardLabel.LabelDomain;
import io.collapp.model.CardLabel.LabelType;
import io.collapp.model.CardLabelValue.LabelValue;
import io.collapp.service.BoardColumnRepository;
import io.collapp.service.BoardRepository;
import io.collapp.service.CardDataService;
import io.collapp.service.CardLabelRepository;
import io.collapp.service.CardRepository;
import io.collapp.service.CardService;
import io.collapp.service.ProjectService;
import io.collapp.service.UserRepository;
import io.collapp.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class ProjectServiceFindRelatedTest {

	private static final String BOARD_SHORTNAME_TEST_SHORT = "TESTSHRT";

	private static final String PROJECT_SHORT_NAME_TEST_FIND = "TESTFIND";

	@Autowired
	private ProjectService projectService;

	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardColumnRepository boardColumnRepository;
	@Autowired
	private CardService cardService;
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CardDataService cardDataService;
	@Autowired
	private CardLabelRepository cardLabelRepository;

	private Project project;
	private Board board;
	private BoardColumn column;
	private Card card;
	private CardData cardData;
	private CardLabel cardLabel;
	private CardLabelValue cardLabelValue;

    private CardLabel cardLabelList;
    private LabelListValue labelListValue;

	@Before
	public void prepare() {
		project = projectService.create("test-findrelated", PROJECT_SHORT_NAME_TEST_FIND, "desc");
		boardRepository.createNewBoard("test", BOARD_SHORTNAME_TEST_SHORT, "desc", project.getId());
		board = boardRepository.findBoardByShortName(BOARD_SHORTNAME_TEST_SHORT);

		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		column = boardColumnRepository.addColumnToBoard("test", definitions.get(0).getId(), BoardColumnLocation.BOARD,
				board.getId());

		Helper.createUser(userRepository, "test", "test-findrelated");
		User user = userRepository.findUserByName("test", "test-findrelated");

		card = cardService.createCard("test", column.getId(), new Date(), user);

		cardData = cardDataService.createComment(card.getId(), "comment", new Date(), user.getId());

		cardLabel = cardLabelRepository.addLabel(project.getId(), false, LabelType.STRING, LabelDomain.USER, "test",
				0xffffff);

		cardLabelRepository.addLabelValueToCard(cardLabel, card.getId(), new LabelValue("test"));
		cardLabelValue = cardLabelRepository.findCardLabelValuesByBoardId(board.getId(), BoardColumnLocation.BOARD)
				.get(card.getId()).get(cardLabel).get(0);

		cardLabelList = cardLabelRepository.addLabel(project.getId(), true, LabelType.LIST, LabelDomain.USER, "list", 0);
		labelListValue = cardLabelRepository.addLabelListValue(cardLabelList.getId(), "value1");
	}

	@Test
	public void testFindNothing() {
		Assert.assertNull(projectService.findRelatedProjectShortNameByBoardShortname("HAHA-SHORT"));
		Assert.assertNull(projectService.findRelatedProjectShortNameByCardDataId(Integer.MAX_VALUE));
		Assert.assertNull(projectService.findRelatedProjectShortNameByCardId(Integer.MAX_VALUE));
		Assert.assertNull(projectService.findRelatedProjectShortNameByColumnId(Integer.MAX_VALUE));
		Assert.assertNull(projectService.findRelatedProjectShortNameByLabelId(Integer.MAX_VALUE));
		Assert.assertNull(projectService.findRelatedProjectShortNameByLabelValueId(Integer.MAX_VALUE));
	}

	@Test
	public void testFindRelatedProjectShortNameByBoardShortname() {
		Assert.assertEquals(project.getShortName(),
				projectService.findRelatedProjectShortNameByBoardShortname(BOARD_SHORTNAME_TEST_SHORT));
	}

	@Test
	public void testFindRelatedProjectShortNameByCardDataId() {
		Assert.assertEquals(project.getShortName(),
				projectService.findRelatedProjectShortNameByCardDataId(cardData.getId()));
	}

	@Test
	public void testFindRelatedProjectShortNameByCardId() {
		Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByCardId(card.getId()));
	}

	@Test
	public void testFindRelatedProjectShortNameByColumnId() {
		Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByColumnId(column.getId()));
	}

	@Test
	public void testFindRelatedProjectShortNameByLabelId() {
		Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByLabelId(cardLabel.getId()));
	}

	@Test
	public void testFindRelatedProjectShortNameByLabelValueId() {
		Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByLabelValueId(cardLabelValue.getCardLabelValueId()));
	}

	@Test
    public void testFindRelatedProjectShortNameByEventId() {
	    int eventId = cardRepository.fetchAllActivityByCardId(card.getId()).get(0).getId();
	    Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByEventId(eventId));
	}


	@Test
    public void testFindRelatedProjectShortNameByLabelListValudIdPath() {
	    Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByLabelListValudIdPath(labelListValue.getId()));
	}

	@Test
    public void testFindRelatedProjectShortNameByColumnDefinitionId() {
	    Assert.assertEquals(project.getShortName(), projectService.findRelatedProjectShortNameByColumnDefinitionId(column.getDefinitionId()));
	}

}
