

var SearchPage = React.createClass({
  notify: function(msg) {

  },
  // handleAnnotationsQuery: function(doc_ids, docs, keywords) {
  //    $.ajax({
  //      url: 'annotations?doc_ids=' + encodeURIComponent(doc_ids),
  //      success: function(data) {
  //        console.log('got annotations')
  //        console.log(data)
  //        this.setState({data: docs, keywords:keywords, annotations:data});
  //        //this.setState({annotations:data});
  //      }.bind(this),
  //      error: function(xhr, status, err) {
  //       console.error(this.props.url, status, err.toString());
  //      }.bind(this)
  //    });
  // },
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
        // var doc_ids = data.map(function(r) { return r['_id']})
        // this.handleAnnotationsQuery(doc_ids, data, keywords)
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
  handleLoadExtractors: function() {
    $.ajax({
      url: 'annotators',
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
  getInitialState: function() {
    return {data: [], extractors: [], keywords: '', annotations:[]};
  },
  componentDidMount: function() {
    this.handleLoadExtractors()
    this.handleKeywordQuery('')
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
        <Results data={this.props.data} />
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
    if (!this.props.data.active)
      classes += ' facet-inactive';
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
    // var docAnnotations = {}
    // this.props.data.map(function(result) { docAnnotations[result['_id']] = [] });

    // this.props.annotations.map(function(a) {
    //   var src = a['_source']
    //   var docId = src['range']['doc_id']
    //   if (docId in docAnnotations) {
    //     docAnnotations[docId].push(src)
    //   }
    // })
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
  getInitialState: function() {
   return {layers: [
     { name: "Tokens", active: false },
     { name: "Sentences", active: false },
     { name: "Lemmas", active: false },
     { name: "PartOfSpeech", active: false },
     { name: "Extractors", active: true },
     { name: "Details", active: false },
     ]};
  },
  onLayerChange: function(name, active) {
    $.each(this.state.layers, function(index, value) {
      if (value.name == name)
        value.active = active; 
    })
    if (this.isMounted()) {
      this.setState({layers:this.state.layers}); 
    }
  },
  render: function() {
    return (<div className='result'>
          <TextWithAnnotations data={this.props.data} layers={this.state.layers} />
          <AnnotationsSelector layers={this.state.layers} onLayerChange={this.onLayerChange} />
      </div>);
  }  
})


React.render(
  <SearchPage />,
  document.getElementById('page')
);
