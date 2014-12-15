package org.sm.lab.mybooks.client.activity;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

import net.customware.gwt.dispatch.client.DispatchAsync;

import org.sm.lab.mybooks.client.AppGinjector;
import org.sm.lab.mybooks.client.event.BookChangedEvent;
import org.sm.lab.mybooks.client.event.BookChangedEvent.Action;
import org.sm.lab.mybooks.client.event.NoteChangedEvent;
import org.sm.lab.mybooks.client.event.NoteChangedEventHandler;
import org.sm.lab.mybooks.client.place.BookFormPlace;
import org.sm.lab.mybooks.client.place.NoteFormPlace;
import org.sm.lab.mybooks.client.ui.BookFormView;
import org.sm.lab.mybooks.client.ui.BookListView;
import org.sm.lab.mybooks.client.util.AppAsyncCallback;
import org.sm.lab.mybooks.client.util.IAppDialogBox;
import org.sm.lab.mybooks.shared.action.CreateBookAction;
import org.sm.lab.mybooks.shared.action.CreateBookResult;
import org.sm.lab.mybooks.shared.action.DeleteBookAction;
import org.sm.lab.mybooks.shared.action.DeleteBookResult;
import org.sm.lab.mybooks.shared.action.LoadAllNotesAction;
import org.sm.lab.mybooks.shared.action.LoadAllNotesResult;
import org.sm.lab.mybooks.shared.action.UpdateBookAction;
import org.sm.lab.mybooks.shared.action.UpdateBookResult;
import org.sm.lab.mybooks.shared.dto.BookDto;
import org.sm.lab.mybooks.shared.dto.NoteDto;
import org.sm.lab.mybooks.shared.validation.ClientGroup;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

public class BookFormActivity extends AbstractActivity implements BookFormView.Presenter, NoteChangedEventHandler {

	private BookDto dto;
	private ListDataProvider<NoteDto> noteTableDataProvider = new ListDataProvider<NoteDto>();
	private final DispatchAsync dispatchRpcService;
	private final EventBus eventBus;
	private final BookListView listView;
	private final BookFormView view;
	private final IAppDialogBox appDialogBox;
	private final PlaceController placeController;

	@Inject
	public BookFormActivity(AppGinjector ginjector) {
	    Log.debug("BookFormActivity.BookFormActivity()");
	    
        this.dispatchRpcService = ginjector.getDispatchAsync();
        this.eventBus = ginjector.getEventBus();
        this.appDialogBox = ginjector.getAppDialogBox();
        this.placeController = ginjector.getPlaceController();
		
		this.listView = ginjector.getBookListView();
        this.view = listView.getBookFormView();
        this.view.setPresenter(this);
		
		noteTableDataProvider.addDataDisplay(view.getNoteTable());
		
		eventBus.addHandler(NoteChangedEvent.TYPE, this);
	}
	
	public BookDto getDto() {
		return this.dto;
	}
	
    @Override
    public void start(AcceptsOneWidget container, EventBus eventBus) {
        BookFormPlace place = (BookFormPlace)placeController.getWhere();
        
        dto = place.getBookDto();
        
        if (dto != null) {
            
            setValues(view, dto);
            
            fetchNoteList();

        }
        
        view.getErrorLabel().setText("");
        view.setVisible(true);
        
        setEnabled();  
        
        container.setWidget(listView.asWidget());
    }
	
	@Override
	public void onUpdateButtonClicked() {
		doSave();
	}

	@Override
	public void onDeleteButtonClicked() {
		doDelete();
	}
	
	@Override
	public void onNewNoteButtonClicked() {
	    Log.debug("BookFormActivity.onNewNoteButtonClicked()");
	    
	    NoteDto noteDto = new NoteDto();
	    noteDto.setBook(dto);
	    
	    placeController.goTo(new NoteFormPlace(noteDto));
	}
	
    @Override
    public void onNoteItemClicked(NoteDto noteDto) {
        Log.debug("BookFormActivity.onNoteItemClicked()");

        noteDto.setBook(dto);

        placeController.goTo(new NoteFormPlace(noteDto));
    }
	

	private void doSave() {
	    
	    getValues(view, dto);
		
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<BookDto>> violations = validator.validate(dto, Default.class, ClientGroup.class);
        
        view.getErrorLabel().setText("");
        
        if (!violations.isEmpty()) {
            StringBuffer errorMessage = new StringBuffer();
            for (ConstraintViolation<BookDto> constraintViolation : violations) {
                if (errorMessage.length() == 0) {
                    errorMessage.append('\n');
                }
                errorMessage.append(constraintViolation.getMessage());
            }
            view.getErrorLabel().setText(errorMessage.toString());
            return;
        }
		
		if (dto.getId() == null) {
			dispatchRpcService.execute(new CreateBookAction(dto), new AppAsyncCallback<CreateBookResult>(eventBus, appDialogBox) {
	            public void onSuccess(CreateBookResult result) {
	            	Log.debug("CreateBookAction -- onSuccess()");
	            	dto = result.getDto();
					eventBus.fireEvent(new BookChangedEvent(Action.CREATED, dto));
	            }
	        });
		} else {
			dispatchRpcService.execute(new UpdateBookAction(dto), new AppAsyncCallback<UpdateBookResult>(eventBus, appDialogBox) {
	            public void onSuccess(UpdateBookResult result) {
	            	Log.debug("UpdateBookAction -- onSuccess()");
	            	eventBus.fireEvent(new BookChangedEvent(Action.UPDATED, dto));
	            }
	        });
		}
		
	}

	private void doDelete() {
	    getValues(view, dto);
	    
		dispatchRpcService.execute(new DeleteBookAction(dto), new AppAsyncCallback<DeleteBookResult>(eventBus, appDialogBox) {
            public void onSuccess(DeleteBookResult result) {
            	Log.debug("DeleteBookAction -- onSuccess()");
            	eventBus.fireEvent(new BookChangedEvent(Action.DELETED, dto));
            }
        });
	}
	
	private void fetchNoteList() {
		dispatchRpcService.execute(new LoadAllNotesAction(dto.getId()) , new AppAsyncCallback<LoadAllNotesResult>(eventBus, appDialogBox) {
            public void onSuccess(LoadAllNotesResult result) {
            	Log.debug("LoadAllNotesResult -- onSuccess()");
            	noteTableDataProvider.setList(result.getDtos());
            	noteTableDataProvider.refresh();
            }
        });

	}
	
    private void getValues(BookFormView view, BookDto dto) {
        dto.setTitle(view.getBookTitle().getValue());
        dto.setAuthor(view.getAuthor().getValue());
        dto.setUrl(view.getUrl().getValue());
//        dto.setStartReadingDate(view.getStartReadingDate());
//        dto.setEndReadingDate(view.getEndReadingDate());
        dto.setRating(dto.getRating());
        
    }

    private void setValues(BookFormView view, BookDto dto) {
        view.getBookTitle().setValue(dto.getTitle());
        view.getAuthor().setValue(dto.getAuthor());
        view.getUrl().setValue(dto.getUrl());
        view.getStartReadingDate().setValue(dto.getStartReadingDate().toString());
        view.getEndReadingDate().setValue(dto.getEndReadingDate().toString());
        view.getRating().setValue(dto.getRating().toString());
    }

	
	public void addNoteInTable(NoteDto noteDto) {
		List<NoteDto> notes = noteTableDataProvider.getList();

		notes.remove(noteDto);
		notes.add(noteDto);

		refreshNoteTable();
	}
	
	public void removeNoteFromTable(NoteDto noteDto) {
		List<NoteDto> notes = noteTableDataProvider.getList();
		notes.remove(noteDto);
		
		refreshNoteTable();
	}
	
	public void refreshNoteTable() {
		noteTableDataProvider.refresh();
	}

	@Override
	public void onNoteUpdated(NoteChangedEvent event) {
		NoteChangedEvent.Action action = event.getAction();
		NoteDto noteDto = event.getChangedNote();

		if (action == NoteChangedEvent.Action.CREATED) {
			addNoteInTable(noteDto);
		} else if (action == NoteChangedEvent.Action.UPDATED) {
			refreshNoteTable();
		} else if (action == NoteChangedEvent.Action.DELETED) {
			removeNoteFromTable(noteDto);
		}
	}
	
	
	private void setEnabled() {
	    boolean enabled = false;
        
	    if (dto != null && dto.getId() != null && dto.getId() > 0) {
	        enabled = true;
        }
        
	    Log.debug("BookFormActivity.setEnabled enabled=" + enabled);
	    
        view.setNewNoteButton(enabled);
        view.setDeleteButtonEnable(enabled);              
        
	}


}
