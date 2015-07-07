
var SearchPage = React.createClass({
  notify: function(msg) {

  },
  handleKeywordQuery: function(keywords) {
    facets = []
    $.each(this.state.extractors, function(index, value) {
      if (value.active)
        facets.push(value.name); });
    $.ajax({
      url: 'docs?keywords=' + encodeURIComponent(keywords) + 
            '&facets=' + facets.join(),
      success: function(data) {
        this.setState({data: data, keywords:keywords});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  handleFacetChange: function(name, active) {
    $.each(this.state.extractors, function(index, value) {
      if (value.name == name)
        value.active = active; });
    this.handleKeywordQuery(this.state.keywords);
  },
  getInitialState: function() {
    return {data: [], extractors: [], keywords: ''};
  },
  componentDidMount: function() {
    $.ajax({
      url: 'extractors',
      success: function(data) {
        // add a field to represent active/non-active
        extractors = data.map(function(it) {
          return {
            'name': it._source.name,
            'active': false
          };
        })
        if (this.isMounted()) {
          this.setState({extractors:extractors}); 
        }
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render: function() {
    return (
      <div>
        <Header onKeywordQuery={this.handleKeywordQuery} />
        <NotificationBox />
        <Content style={{'height':'100%'}} 
          data={this.state.data} 
          extractors={this.state.extractors} 
          onFacetChange={this.handleFacetChange} />
      </div>
    );
  }
});

var Header = React.createClass({
  setFocus: function() {
    React.findDOMNode(this.refs.query).focus();
  },
  componentDidMount: function() {
    this.setFocus();
  },
  getInitialState: function() {
    return {query: ''};
  },
  inputSubmit: function() {
    var val = this.state.query; //refs.query.getDOMNode().value;
    this.props.onKeywordQuery(val)
  },
  handleChange: function(evt) {
    this.setState({query: evt.target.value});
  },
  handleKeyDown: function(evt) {
      if (evt.keyCode == 13 ) {
          return this.inputSubmit();
      }
  },
  render: function() {
    return (
      <div className='header unselectable'>
        <div style={{position:'absolute', top:0, left:0, marginLeft:'10px', marginTop:'10px', cursor:'pointer' }}>
          <span style={{fontFamily:'Open Sans, sans-serif', fontSize:'22px'}}></span>
        </div>
        <div style={{position:'absolute', top:0, left:200}}>
          <input type='text' ref='query'  value={this.state.query}
            onChange={this.handleChange} onKeyDown={this.handleKeyDown}/>
        </div>
      </div>
    );
  }
});

var NotificationBox = React.createClass({
  render: function() {
    return (<div></div>);
  }
})

var Content = React.createClass({
  render: function() {
    return (
      <div className='content'>
        <LeftMenu extractors={this.props.extractors}
          onFacetChange={this.props.onFacetChange} />
        <Results data={this.props.data}/>
        <Help />
      </div>
      );
  }
})

var LeftMenu = React.createClass({
  render: function() {
    var onFacetChange = this.props.onFacetChange;
    var extractorNodes = this.props.extractors.map(function(ex) {
      return (
        <Facet data={ex} onFacetChange={onFacetChange} />
        );
    });
    return (<div className='leftmenu'><br />
      {extractorNodes}
      </div>);
  }
})

var Facet = React.createClass({
  handleClick: function() {
    var active = !this.props.data.active;
    this.props.onFacetChange(this.props.data.name, active);
  },
  render: function() {
    var classes = 'facet';
    if (this.props.data.active)
      classes += ' facet-active';
    return (<div className={classes} onClick={this.handleClick}>
       <div style={{display:'inline-block',width:'30px'}}>
         <i className="fa fa-check" ></i>
       </div> 
       {this.props.data.name}</div>)
  }
})

var Help = React.createClass({
  render: function() {
    return (<div className='help'></div>);
  }
})

var Results = React.createClass({
  render: function() {
    var resultNodes = this.props.data.map(function(result) {
      return (
        <Result data={result} />
        );
    });
    return (<div style={{marginLeft:'200px', marginRight:'200px'}}>
      <br />
      {resultNodes}
      </div>);
  }
})

var Result = React.createClass({
  findArgs: function(snt) {
   //TODO    
  },
  render: function() {
    content = this.props.data._source.content;
    // if we have field with keyword highlighting, take that
    if (this.props.data.highlight != null &&
        this.props.data.highlight.content != null) {
      content = this.props.data.highlight.content[0];
    }
    var extractions = []
    $.each(this.props.data._source, function(name, value) {
      if (name != 'content' && name != 'id' && value instanceof Array)
        extractions.push (<div className='extraction'>{name} : {value.join(', ')}</div>);
    })

    return (<div className='result'>
             <div>
          <span dangerouslySetInnerHTML={{__html: content}} />
             </div>
             {extractions}
      </div>);
  }  
})




React.render(
  <SearchPage />,
  document.getElementById('page')
);
